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
    private CountDownLatch heatingLatch;
    private CountDownLatch timerLatch;

    private ScheduledExecutorService sensorDataScheduler;
    private ScheduledExecutorService repeatingMessageScheduler;
    private ScheduledExecutorService timerScheduler;
    private final AtomicBoolean repeatingMessageActive = new AtomicBoolean(false);
    private final AtomicBoolean stopRecipe = new AtomicBoolean(false);

    public void startBrewing(int recipeId) throws IOException {
        try {
            stopRecipe.set(false);
            startSensorDataLogging();
            while (!stopRecipe.get()) {
                var brewResponseDto = brewService.nextBrewingStep(recipeId);
                if (brewResponseDto.getHeatingTemperature() != null &&
                        brewResponseDto.getDoughingDtoList() != null && !brewResponseDto.getDoughingDtoList().isEmpty()) {
                    int targetTemperature = brewResponseDto.getHeatingTemperature();
                    heatingToTargetTemperature(targetTemperature);
                    if (stopRecipe.get()) break;

                    String doughingToMessage = fillStringOfDoughing(brewResponseDto.getDoughingDtoList());
                    sendRepeatingMessage(new BrewingMessage(MessageType.DOUGHING, "Nasypte: " + doughingToMessage, nextStepBuilder(brewResponseDto)));
                    log.debug("Doughing {}", doughingToMessage);
                    waitForUserInteractionOnDoughing();
                } else if (brewResponseDto.getActualStep() != null) {
                    int targetTemperature = brewResponseDto.getActualStep().getTemperature();
                    heatingToTargetTemperature(targetTemperature);
                    if (stopRecipe.get()) break;

                    hardwareControlService.turnOnHeater(targetTemperature);

                    int countDownTimeInSeconds = brewResponseDto.getActualStep().getDuration() * 60;
                    startCountdownTimer(countDownTimeInSeconds);
                } else if (Boolean.TRUE.equals(brewResponseDto.getOverpumping())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.OVERPUMPING, "Prečerpajte: V hlavnej nádobe má byť " + brewResponseDto.getOverpumpingPercentage() + "%", nextStepBuilder(brewResponseDto)));
                    log.debug("Overpumping brewing step. In main kettle should be {}%", brewResponseDto.getOverpumpingPercentage());
                    hardwareControlService.turnOffHeater();
                    waitForUserInteractionOnOverpumping();
                } else if (Boolean.TRUE.equals(brewResponseDto.getLautering())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.LAUTERING, "Vyslaďte", nextStepBuilder(brewResponseDto)));
                    log.debug("Lautering");
                    hardwareControlService.turnOffHeater();
                    waitForUserInteractionOnLautering();
                } else if (Boolean.TRUE.equals(brewResponseDto.getCooling())) {
                    sendRepeatingMessage(new BrewingMessage(MessageType.COOLING, "Schlaďte na teplotu kvasenia", nextStepBuilder(brewResponseDto)));
                    log.debug("Cooling");
                    hardwareControlService.turnOffHeater();
                } else if (stopRecipe.get()) {
                    break;
                }
            }
        }
        finally {
            cleanupResources();
        }
    }

    private void cleanupResources() {
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
        brewLog.setMashWeight(sensorsResponseDto.getMashWeight());
        brewLog.setMashTemperature(sensorsResponseDto.getMashTemperature());
        brewLog.setWorthTemperature(sensorsResponseDto.getWorthTemperature());
        brewLog.setTimestamp(LocalDateTime.now());
        brewLog.setPower(sensorsResponseDto.getPower());
        return brewLog;
    }

    private void stopSensorDataLogging() {
        if (sensorDataScheduler != null && !sensorDataScheduler.isShutdown()) {
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
        timerLatch = new CountDownLatch(1);

        timerScheduler = Executors.newScheduledThreadPool(1);
        timerScheduler.scheduleAtFixedRate(() -> {
            int seconds = remainingSeconds.decrementAndGet();
            if (seconds >= 0 && !stopRecipe.get()) {
                messagingTemplate.convertAndSend("/topic/brewing", new BrewingMessage(MessageType.TIMER, formatTime(seconds), ""));

                if (seconds == 0) {
                    timerLatch.countDown();
                    stopTimerScheduler();
                }
            } else if (stopRecipe.get() || seconds < 0) {
                timerLatch.countDown();
                stopTimerScheduler();
            }
        }, 0, 1, TimeUnit.SECONDS);

        try {
            timerLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void stopTimerScheduler() {
        if (timerScheduler != null && !timerScheduler.isShutdown()) {
            timerScheduler.shutdown();
            try {
                if (!timerScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    timerScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                timerScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            timerScheduler = null;
        }
    }

    private static String formatTime(int totalSeconds) {
        Duration duration = Duration.ofSeconds(totalSeconds);
        long minutes = duration.toMinutes();
        long seconds = duration.toSecondsPart();
        return String.format("%d:%02d", minutes, seconds);
    }

    private void heatingToTargetTemperature(int targetTemperature) throws IOException {
        sendRepeatingMessage(new BrewingMessage(MessageType.HEATING, "Zohrievam na teplotu " + targetTemperature + "°C", ""));
        log.debug("Heating to temperature: {}°C", targetTemperature);

        hardwareControlService.turnOnHeater(targetTemperature);
        waitForTargetTemperature(targetTemperature);
    }

    private void sendRepeatingMessage(BrewingMessage message) {
        if (repeatingMessageActive.compareAndSet(false, true)) {
            repeatingMessageScheduler = Executors.newScheduledThreadPool(1);
            repeatingMessageScheduler.scheduleAtFixedRate(() -> messagingTemplate.convertAndSend("/topic/brewing", message), 0, 5, TimeUnit.SECONDS);
        }
    }

    private void stopRepeatingMessage() {
        if (repeatingMessageActive.compareAndSet(true, false) && repeatingMessageScheduler != null && !repeatingMessageScheduler.isShutdown()) {
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
        heatingLatch = new CountDownLatch(1);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (stopRecipe.get()) {
                    heatingLatch.countDown();
                    scheduler.shutdown();
                    stopRepeatingMessage();
                    return;
                }

                var sensorsData = hardwareControlService.getSensorsData();
                var currentTemperature = sensorsData.getMashTemperature();
                if (currentTemperature >= (targetTemperature - 0.5)) {
                    heatingLatch.countDown();
                    scheduler.shutdown();
                    stopRepeatingMessage();
                    log.debug("Temperature saving stopped.");
                }
            } catch (IOException | InterruptedException e) {
                log.error("Error getting sensor data during saving.");
            }
        }, 0, 2, TimeUnit.SECONDS);

        try {
            heatingLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
                try {
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
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

    public void stop() throws IOException {
        log.debug("Stopping brewing process...");
        stopRecipe.set(true);
        hardwareControlService.turnOffHeater();

        if (doughingLatch != null) {
            doughingLatch.countDown();
        }
        if (overpumpingLatch != null) {
            overpumpingLatch.countDown();
        }
        if (lauteringLatch != null) {
            lauteringLatch.countDown();
        }
        if (heatingLatch != null) {
            heatingLatch.countDown();
        }
        if (timerLatch != null) {
            timerLatch.countDown();
        }

        stopRepeatingMessage();
        stopSensorDataLogging();
        stopTimerScheduler();

        messagingTemplate.convertAndSend("/topic/brewing",
                new BrewingMessage(MessageType.STOP, "Varenie bolo zastavené", ""));

        log.debug("Brewing process has been forcibly stopped.");

        // Dôležité pridanie
        cleanupResources();
    }

}