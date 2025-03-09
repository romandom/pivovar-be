package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.arduino.HardwareControlService;
import cz.diplomka.pivovar.constant.BrewingPhase;
import cz.diplomka.pivovar.constant.BrewingStatus;
import cz.diplomka.pivovar.dto.BrewResponseDto;
import cz.diplomka.pivovar.dto.DoughingDto;
import cz.diplomka.pivovar.dto.SensorsResponseDto;
import cz.diplomka.pivovar.dto.StepDto;
import cz.diplomka.pivovar.model.*;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class BrewService {

    private final RecipeRepository recipeRepository;
    private final BrewSessionRepository brewSessionRepository;

    private final HardwareControlService hardwareControlService;


    public BrewResponseDto nextBrewingStep(int recipeId) {
        final Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found"));

        return recipe.getBrewSessions().stream()
                .filter(bs -> bs.getStatus() == BrewingStatus.IN_PROGRESS)
                .findFirst()
                .map(brewingSession -> {
                    try {
                        return switch (brewingSession.getBrewingPhase()) {
                            case STARTED -> handleStartedPhase(recipe, brewingSession);
                            case MASHING -> handleMashingPhase(recipe, brewingSession);
                            case BOILING -> handleBoilingPhase(recipe, brewingSession);
                        };
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException("Error processing brewing step", e);
                    }
                })
                .orElseGet(() -> {
                    try {
                        return handleNullBrewingSession(recipe);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException("Error handling null brewing session", e);
                    }
                });
    }


    private BrewResponseDto handleNullBrewingSession(Recipe recipe) throws IOException, InterruptedException {
        final BrewSession brewingSession = BrewSession.builder()
                .startTime(LocalDateTime.now())
                .currentStep(0)
                .brewingPhase(BrewingPhase.STARTED)
                .status(BrewingStatus.IN_PROGRESS)
                .build();
        recipe.getBrewSessions().add(brewingSession);
        recipeRepository.save(recipe);

        final int heatingTemperature = getMashingStepByStepNumber(recipe, 1).getTemperature();

        final List<DoughingDto> doughingDtoList = recipe.getIngredient().getMalts()
                .stream()
                .map(malt -> DoughingDto.builder().name(malt.getName()).weight(malt.getWeight()).build())
                .toList();

        //hardwareControlService.turnOnHeater(heatingTemperature);

        return BrewResponseDto.builder()
                .heatingTemperature(heatingTemperature)
                .doughingDtoList(doughingDtoList)
                .build();
    }

    private BrewResponseDto handleStartedPhase(Recipe recipe, BrewSession brewingSession) throws IOException, InterruptedException {
        brewingSession.setCurrentStep(1);
        brewingSession.setBrewingPhase(BrewingPhase.MASHING);
        brewSessionRepository.save(brewingSession);

        //hardwareControlService.turnOnMashMixing();

        final MashingStep actualStep = getMashingStepByStepNumber(recipe, 1);
        final MashingStep nextStep = getMashingStepByStepNumber(recipe, 2);

        final BrewResponseDto.BrewResponseDtoBuilder responseBuilder = BrewResponseDto.builder()
                .actualStep(buildStepDtoForMashing(actualStep))
                .brewingPhase(BrewingPhase.MASHING);

        if (nextStep != null) {
            responseBuilder.nextStep(buildStepDtoForMashing(nextStep));
        }

        return responseBuilder.build();
    }


    private BrewResponseDto handleMashingPhase(Recipe recipe, BrewSession brewingSession) throws IOException, InterruptedException {
        int currentStepNumber = brewingSession.getCurrentStep();
        final MashingStep nextStep = getMashingStepByStepNumber(recipe, currentStepNumber + 1);

        if (nextStep == null) {
            brewingSession.setBrewingPhase(BrewingPhase.BOILING);
            brewingSession.setCurrentStep(0);
            brewSessionRepository.save(brewingSession);

            return BrewResponseDto.builder().lautering(true).build();
        }

        final MashingStep currentStep = recipe.getMashingSteps().stream()
                .filter(step -> step.getStepNumber() == currentStepNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Current mashing step not found"));

        boolean overPumping = (currentStep.getPercentage() != 100 && nextStep.getPercentage() == 100) ||
                (currentStep.getPercentage() == 100 && nextStep.getPercentage() != 100);
        Integer decoctionTemperature = null;

        if (!overPumping) {
            if (currentStep.getPercentage() != 100) {
                for (int i = currentStepNumber - 1; i >= 0; i--) {
                    final MashingStep previousStep = getMashingStepByStepNumber(recipe, i);
                    if (previousStep != null && previousStep.getPercentage() == 100) {
                        decoctionTemperature = previousStep.getTemperature();
                        break;
                    }
                }
            } else {
                decoctionTemperature = currentStep.getTemperature();
            }
        }

        if (overPumping && !brewingSession.isOverPumping()) {
            brewingSession.setOverPumping(true);
            brewSessionRepository.save(brewingSession);

            return BrewResponseDto.builder()
                    .brewingPhase(BrewingPhase.MASHING)
                    .overpumpingPercentage(nextStep.getPercentage())
                    .overpumping(true)
                    .build();
        }

        final MashingStep afterNextStep = getMashingStepByStepNumber(recipe, currentStepNumber + 2);
        //hardwareControlService.turnOnHeater(nextStep.getTemperature());

        brewingSession.setBrewingPhase(BrewingPhase.MASHING);
        brewingSession.setCurrentStep(currentStepNumber + 1);
        brewingSession.setOverPumping(false);
        brewSessionRepository.save(brewingSession);

        final BrewResponseDto.BrewResponseDtoBuilder responseBuilder = BrewResponseDto.builder()
                .brewingPhase(BrewingPhase.MASHING)
                .decoctionTemperature(decoctionTemperature)
                .actualStep(buildStepDtoForMashing(nextStep));

        if (afterNextStep != null) {
            responseBuilder.nextStep(buildStepDtoForMashing(afterNextStep));
        }

        return responseBuilder.build();
    }


    private BrewResponseDto handleBoilingPhase(Recipe recipe, BrewSession brewingSession) throws IOException, InterruptedException {
        final int currentStepNumber = brewingSession.getCurrentStep();
        final HoppingStep nextStep = getHoppingStepByStepNumber(recipe, currentStepNumber + 1);

        if (nextStep == null) {
            //hardwareControlService.turnOffHeater();

            brewingSession.setBrewingPhase(BrewingPhase.BOILING);
            brewingSession.setCurrentStep(0);
            brewingSession.setEndTime(LocalDateTime.now());
            brewingSession.setStatus(BrewingStatus.COMPLETED);
            brewSessionRepository.save(brewingSession);

            return BrewResponseDto.builder()
                    .cooling(true)
                    .build();
        }

        brewingSession.setBrewingPhase(BrewingPhase.BOILING);
        brewingSession.setCurrentStep(currentStepNumber + 1);
        brewSessionRepository.save(brewingSession);

        //hardwareControlService.turnOnHeater(100);
       // hardwareControlService.turnOffMashMixing();

        final HoppingStep firstStep = getHoppingStepByStepNumber(recipe, 1);
        final HoppingStep afterNextStep = getHoppingStepByStepNumber(recipe, currentStepNumber + 2);

        BrewResponseDto.BrewResponseDtoBuilder responseBuilder = BrewResponseDto.builder()
                .heatingTemperature(100)
                .brewingPhase(BrewingPhase.BOILING);

        if (firstStep != null) {
            responseBuilder.boilingTime(firstStep.getTime());
        }

        responseBuilder.actualStep(buildStepDtoForHopping(nextStep));

        if (afterNextStep != null) {
            responseBuilder.nextStep(buildStepDtoForHopping(afterNextStep));
        }

        return responseBuilder.build();
    }


    private StepDto buildStepDtoForHopping(HoppingStep hoppingStep) {
        return StepDto.builder()
                .stepNumber(hoppingStep.getStepNumber())
                .duration(hoppingStep.getTime())
                .name(hoppingStep.getName())
                .build();
    }

    private StepDto buildStepDtoForMashing(MashingStep mashingStep) {
        return StepDto.builder()
                .stepNumber(mashingStep.getStepNumber())
                .percentage(mashingStep.getPercentage())
                .duration(mashingStep.getTime())
                .temperature(mashingStep.getTemperature())
                .build();
    }

    private HoppingStep getHoppingStepByStepNumber(Recipe recipe, int stepNumber) {
        return recipe.getHoppingSteps().stream()
                .filter(step -> Objects.equals(step.getStepNumber(), stepNumber))
                .findFirst()
                .orElse(null);
    }

    private MashingStep getMashingStepByStepNumber(Recipe recipe, int stepNumber) {
        return recipe.getMashingSteps().stream()
                .filter(step -> Objects.equals(step.getStepNumber(), stepNumber))
                .findFirst()
                .orElse(null);
    }

    public Boolean checkBrewing(int recipeId) {
        final Recipe recipe = recipeRepository.findById(recipeId).orElseThrow();
        final BrewSession brewSession = recipe.getBrewSessions()
                .stream()
                .filter(bs -> bs.getStatus().equals(BrewingStatus.IN_PROGRESS))
                .findFirst()
                .orElse(null);

        if (brewSession == null) {
            return false;
        }

        if (isOlderThanSixHours(brewSession)) {
            brewSession.setEndTime(LocalDateTime.now());
            brewSession.setStatus(BrewingStatus.CANCELLED);
            brewSessionRepository.save(brewSession);
            return false;
        }
        return true;
    }

    private boolean isOlderThanSixHours(BrewSession brewSession) {
        LocalDateTime sixHoursAgo = LocalDateTime.now().minusHours(6);
        return brewSession.getStartTime().isBefore(sixHoursAgo);
    }

    public void saveBrewSensorData(SensorsResponseDto sensorsResponseDto) {
        final BrewLog brewLog = new BrewLog();
        brewLog.setTimestamp(LocalDateTime.now());
        brewLog.setMashTemperature(sensorsResponseDto.getMashTemperature());
        brewLog.setWorthTemperature(sensorsResponseDto.getWorthTemperature());
        brewLog.setMashWeight(sensorsResponseDto.getMashWeight());
        brewLog.setWorthWeight(sensorsResponseDto.getWorthWeight());

        final BrewSession brewSession = brewSessionRepository.findAll()
                .stream()
                .filter(bs -> bs.getStatus().equals(BrewingStatus.IN_PROGRESS))
                .findFirst()
                .orElse(null);

        if (brewSession != null) {
            brewSession.getBrewLogs().add(brewLog);
            brewSessionRepository.save(brewSession);
        }
    }
}
