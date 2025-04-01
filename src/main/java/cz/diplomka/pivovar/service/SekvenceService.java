package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.arduino.HardwareControlService;
import cz.diplomka.pivovar.constant.BrewingStatus;
import cz.diplomka.pivovar.constant.MessageType;
import cz.diplomka.pivovar.dto.*;
import cz.diplomka.pivovar.model.BrewLog;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SekvenceService {

    private final SimpMessagingTemplate messagingTemplate;
    private final BrewService brewService;
    private final HardwareControlService hardwareControlService;
    private final BrewSessionRepository brewSessionRepository;

    private CountDownLatch doughingLatch;
    private CountDownLatch overpumpingLatch;
    private CountDownLatch lauteringLatch;

    private ScheduledExecutorService sensorDataScheduler;
    private ScheduledExecutorService repeatingMessageScheduler;
    private final AtomicBoolean repeatingMessageActive = new AtomicBoolean(false);
    private final AtomicBoolean stopRecipe = new AtomicBoolean(false);

    public void startBrewing(int recipeId) {
        try {
            startSensorDataLogging();
            while (true) {
                var brewResponseDto = brewService.nextBrewingStep(recipeId);
                if (brewResponseDto.getHeatingTemperature() != null && Boolean.FALSE.equals(brewResponseDto.getDoughingDtoList().isEmpty())) {
                    int targetTemperature = brewResponseDto.getHeatingTemperature();
                    heatingToTargetTemperature(targetTemperature);
                    String doughingToMessage = fillStringOfDoughing(brewResponseDto.getDoughingDtoList());
                    sendRepeatingMessage(new BrewingMessage(MessageType.DOUGHING, "Nasypte: " + doughingToMessage, nextStepBuilder(brewResponseDto)));
                    log.debug("Doughing {}", doughingToMessage);
                    waitForUserInteractionOnDoughing();
                } else if (brewResponseDto.getActualStep() != null) {
                    int targetTemperature = brewResponseDto.getActualStep().getTemperature();
                    heatingToTargetTemperature(targetTemperature);

                    int countDownTimeInSeconds = brewResponseDto.getActualStep().getDuration() * 60;
                    startCountdownTimer(countDownTimeInSeconds);
                } else if (Boolean.TRUE.equals(brewResponseDto.getOverpumping())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.OVERPUMPING, "Prečerpajte: V hlavnej nádobe má byť " + brewResponseDto.getOverpumpingPercentage() + "%", nextStepBuilder(brewResponseDto)));
                    log.debug("Overpumping brewing step. In main kettle should be {}%", brewResponseDto.getOverpumpingPercentage());
                    waitForUserInteractionOnOverpumping();
                } else if (Boolean.TRUE.equals(brewResponseDto.getLautering())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.LAUTERING, "Vyslaďte", nextStepBuilder(brewResponseDto)));
                    log.debug("Lautering");
                    waitForUserInteractionOnLautering();
                } else if (Boolean.TRUE.equals(brewResponseDto.getCooling())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.COOLING, "Schlaďte na teplotu kvasenia", nextStepBuilder(brewResponseDto)));
                    log.debug("Cooling");
                    waitForUserInteractionOnLautering();
                } else if (stopRecipe.get()) {
                    break;
                }
            }
        }
        finally {
            stopSensorDataLogging();
            log.debug("Stopping sensor data logging");

            brewSessionRepository.findBrewSessionByStatus(BrewingStatus.IN_PROGRESS)
                    .stream()
                    .findFirst()
                    .ifPresent(brewSession -> {
                        brewSession.setStatus(BrewingStatus.COMPLETED);
                        brewSession.setEndTime(LocalDateTime.now());
                        brewSessionRepository.save(brewSession);
                    });
            log.debug("Brewing completed.");
        }
    }

    private String nextStepBuilder(BrewResponseDto brewResponseDto) {
        if (brewResponseDto.getNextStep() == null) {
            return "";
        }
        return switch (brewResponseDto.getBrewingPhase()) {
            case MASHING -> "Následujúci krok: " +
                    brewResponseDto.getNextStep().getTemperature() + "°C " +
                    brewResponseDto.getNextStep().getDuration() + "minút";
            case BOILING -> "Následujúci krok: " +
                    brewResponseDto.getNextStep().getName() + " " +
                    brewResponseDto.getNextStep().getWeight() +  "g";
            default -> "";
        };
    }

    private String fillStringOfDoughing(List<DoughingDto> doughingDtoList) {
        return doughingDtoList.stream()
                .map(DoughingDto::toString)
                .collect(Collectors.joining(", "));
    }

    private void startSensorDataLogging() {
        sensorDataScheduler = Executors.newScheduledThreadPool(1);
        sensorDataScheduler.scheduleAtFixedRate(() -> {
            try {
                var sensorsData = hardwareControlService.getSensorsData();
                brewSessionRepository.findBrewSessionByStatus(BrewingStatus.IN_PROGRESS)
                        .stream()
                        .findFirst()
                        .ifPresent(bs -> {
                            bs.getBrewLogs().add(createBrewLog(sensorsData));
                            brewSessionRepository.save(bs);
                        });
            } catch (IOException | InterruptedException e) {
                log.error("Sensor data logging error. Cannot find brewing session.");
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private BrewLog createBrewLog(SensorsResponseDto sensorsResponseDto) {
        var brewLog = new BrewLog();
        brewLog.setWorthWeight(sensorsResponseDto.getWorthWeight());
        brewLog.setMashWeight(sensorsResponseDto.getMashWeight());
        brewLog.setMashTemperature(sensorsResponseDto.getMashTemperature());
        brewLog.setWorthTemperature(sensorsResponseDto.getWorthTemperature());
        brewLog.setTimestamp(LocalDateTime.now());
        return brewLog;
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
                    messagingTemplate.convertAndSend("/topic/brewing", new BrewingMessage(MessageType.TIMER, formatTime(seconds), ""));
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
        sendRepeatingMessage(new BrewingMessage(MessageType.HEATING, "Zohrievam na teplotu " + targetTemperature + "°C", ""));
        log.debug("Heating to temperature: {}°C", targetTemperature);
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
                        log.debug("Temperature saving stopped.");
                    }
                } catch (IOException | InterruptedException e) {
                    log.error("Error getting sensor data during saving.");
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
            log.debug("Doughing done.");
        }
    }

    public void overpumpingDone() {
        if (overpumpingLatch != null) {
            overpumpingLatch.countDown();
            stopRepeatingMessage();
            log.debug("Overpumping done.");
        }
    }

    public void lauteringDone() {
        if (lauteringLatch != null) {
            lauteringLatch.countDown();
            stopRepeatingMessage();
            log.debug("Lautering done.");
        }
    }

    public void stop() {
        stopRecipe.set(true);
    }
}
