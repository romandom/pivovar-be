package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.arduino.HardwareControlService;
import cz.diplomka.pivovar.constant.MessageType;
import cz.diplomka.pivovar.dto.BrewingMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
public class SekvenceService {

    private final SimpMessagingTemplate messagingTemplate;
    private final BrewService brewService;
    private final HardwareControlService hardwareControlService;

    private CountDownLatch doughingLatch;
    private CountDownLatch overpumpingLatch;
    private CountDownLatch lauteringLatch;

    private ScheduledExecutorService sensorDataScheduler;
    private ScheduledExecutorService repeatingMessageScheduler;
    private final AtomicBoolean repeatingMessageActive = new AtomicBoolean(false);

    public void startBrewing(int recipeId) {
        startSensorDataLogging();
        try {
            while (true) {
                var brewResponseDto = brewService.nextBrewingStep(recipeId);
                if (brewResponseDto.getHeatingTemperature() != null && !brewResponseDto.getDoughingDtoList().isEmpty()) {
                    int targetTemperature = brewResponseDto.getHeatingTemperature();
                    heatingToTargetTemperature(targetTemperature);

                    sendRepeatingMessage(new BrewingMessage(MessageType.DOUGHING, "Nasypte"));
                    waitForUserInteractionOnDoughing();
                } else if (brewResponseDto.getActualStep() != null) {
                    int targetTemperature = brewResponseDto.getActualStep().getTemperature();
                    heatingToTargetTemperature(targetTemperature);

                    int countDownTimeInSeconds = brewResponseDto.getActualStep().getDuration() * 60;
                    startCountdownTimer(countDownTimeInSeconds);
                } else if (Boolean.TRUE.equals(brewResponseDto.getOverpumping())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.OVERPUMPING, "Prečerpajte"));
                    waitForUserInteractionOnOverpumping();
                } else if (Boolean.TRUE.equals(brewResponseDto.getLautering())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.LAUTERING, "Vyslaďte"));
                    waitForUserInteractionOnLautering();
                } else if (Boolean.TRUE.equals(brewResponseDto.getCooling())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.COOLING, "Schlaďte na teplotu kvasenia"));
                    waitForUserInteractionOnLautering();
                }
            }
        }
        finally {
            stopSensorDataLogging();
        }
    }

    private void startSensorDataLogging() {
        sensorDataScheduler = Executors.newScheduledThreadPool(1);
        sensorDataScheduler.scheduleAtFixedRate(() -> {
            try {
                var sensorsData = hardwareControlService.getSensorsData();
//                sensorsData.setTimestamp(LocalDateTime.now());
//                sensorDataService.save(sensorsData);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void stopSensorDataLogging() {
        if (sensorDataScheduler != null) {
            sensorDataScheduler.shutdown();
            try {
                if (!sensorDataScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    sensorDataScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                sensorDataScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startCountdownTimer(int countdownSeconds) {
        AtomicInteger remainingSeconds = new AtomicInteger(countdownSeconds);
        CountDownLatch timerLatch = new CountDownLatch(countdownSeconds);

        try (ScheduledExecutorService timerScheduler = Executors.newScheduledThreadPool(1)) {
            timerScheduler.scheduleAtFixedRate(() -> {
                int seconds = remainingSeconds.decrementAndGet();
                if (seconds >= 0) {
                    messagingTemplate.convertAndSend("/topic/brewing", new BrewingMessage(MessageType.TIMER, formatTime(seconds)));
                    timerLatch.countDown();
                } else {
                    timerScheduler.shutdown();
                }
            }, 0, 1, TimeUnit.SECONDS);

            try {
                timerLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String formatTime(int totalSeconds) {
        Duration duration = Duration.ofSeconds(totalSeconds);
        long minutes = duration.toMinutes();
        long seconds = duration.toSecondsPart();
        return String.format("%d:%02d", minutes, seconds);
    }

    private void heatingToTargetTemperature(int targetTemperature) {
        sendRepeatingMessage(new BrewingMessage(MessageType.HEATING, "Zohrievam na teplotu " + targetTemperature));
        waitForTargetTemperature(targetTemperature);
    }

    private void sendRepeatingMessage(BrewingMessage message) {
        if (repeatingMessageActive.compareAndSet(false, true)) {
            repeatingMessageScheduler = Executors.newScheduledThreadPool(1);
            repeatingMessageScheduler.scheduleAtFixedRate(() -> messagingTemplate.convertAndSend("/topic/brewing", message), 0, 5, TimeUnit.SECONDS);
        }
    }

    private void stopRepeatingMessage() {
        if (repeatingMessageActive.compareAndSet(true, false) && repeatingMessageScheduler != null) {
            repeatingMessageScheduler.shutdown();
            try {
                if (!repeatingMessageScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    repeatingMessageScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                repeatingMessageScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            repeatingMessageScheduler = null;
        }
    }


    private void waitForTargetTemperature(int targetTemperature) {
        CountDownLatch latch = new CountDownLatch(1);

        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    var sensorsData = hardwareControlService.getSensorsData();
                    var currentTemperature = sensorsData.getMashTemperature();
                    if (currentTemperature >= targetTemperature) {
                        latch.countDown();
                        scheduler.shutdown();
                        stopRepeatingMessage();
                    }
                } catch (IOException | InterruptedException e) {
                    System.err.println("Error getting sensor data: " + e.getMessage());
                }
            }, 0, 2, TimeUnit.SECONDS);

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void waitForUserInteractionOnDoughing() {
        doughingLatch = new CountDownLatch(1);
        try {
            doughingLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForUserInteractionOnOverpumping() {
        overpumpingLatch = new CountDownLatch(1);
        try {
            overpumpingLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitForUserInteractionOnLautering() {
        lauteringLatch = new CountDownLatch(1);
        try {
            lauteringLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void doughingDone() {
        if (doughingLatch != null) {
            doughingLatch.countDown();
            stopRepeatingMessage();
        }
    }

    public void overpumpingDone() {
        if (overpumpingLatch != null) {
            overpumpingLatch.countDown();
            stopRepeatingMessage();
        }
    }

    public void lauteringDone() {
        if (lauteringLatch != null) {
            lauteringLatch.countDown();
            stopRepeatingMessage();
        }
    }
}
