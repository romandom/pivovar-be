package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.arduino.HardwareControlService;
import cz.diplomka.pivovar.constant.BrewingPhase;
import cz.diplomka.pivovar.constant.BrewingStatus;
import cz.diplomka.pivovar.dto.BrewResponseDto;
import cz.diplomka.pivovar.dto.DoughingDto;
import cz.diplomka.pivovar.dto.StepDto;
import cz.diplomka.pivovar.model.BrewSession;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Transactional
public class BrewService {

    private final RecipeRepository recipeRepository;
    private final BrewSessionRepository brewSessionRepository;

    private final HardwareControlService hardwareControlService;


    public BrewResponseDto nextBrewingStep(int recipeId) throws IOException {
        val recipe = recipeRepository.findById(recipeId).orElseThrow();

        val brewingSessions = recipe.getBrewSessions();

        var brewingSession = brewingSessions.stream()
                .filter(bs -> bs.getStatus().equals(BrewingStatus.IN_PROGRESS))
                .findFirst()
                .orElse(null);

        if (brewingSession == null) {
            brewingSession = BrewSession.builder()
                    .startTime(LocalDateTime.now())
                    .currentStep(0)
                    .brewingPhase(BrewingPhase.STARTED)
                    .status(BrewingStatus.IN_PROGRESS)
                    .build();
            recipe.getBrewSessions().add(brewingSession);
            recipeRepository.save(recipe);

            val heatingTemperature = recipe.getMashingSteps()
                    .stream()
                    .filter(mashingStep -> mashingStep.getStepNumber() == 1)
                    .findFirst()
                    .orElseThrow()
                    .getTemperature();

            val doughingDtoList = recipe.getIngredient().getMalts()
                    .stream()
                    .map(malt -> DoughingDto.builder().name(malt.getName()).weight(malt.getWeight()).build())
                    .toList();

            hardwareControlService.turnOnHeater(heatingTemperature);

            return BrewResponseDto.builder()
                    .heatingTemperature(heatingTemperature)
                    .doughingDtoList(doughingDtoList)
                    .build();
        }

        if (brewingSession.getBrewingPhase() == BrewingPhase.STARTED) {
            brewingSession.setCurrentStep(1);
            brewingSession.setBrewingPhase(BrewingPhase.MASHING);

            hardwareControlService.turnOnMashMixing();

            val actualStep = recipe.getMashingSteps().stream().filter(step -> step.getStepNumber() == 1).findFirst().orElseThrow();
            val nextStep = recipe.getMashingSteps().stream().filter(step -> step.getStepNumber() == 2).findFirst().orElse(null);

            val actualStepDto = StepDto.builder()
                    .stepNumber(actualStep.getStepNumber())
                    .percentage(actualStep.getPercentage())
                    .duration(actualStep.getTime())
                    .temperature(actualStep.getTemperature())
                    .build();

            if (nextStep == null) {
                return BrewResponseDto.builder()
                        .actualStep(actualStepDto)
                        .brewingPhase(BrewingPhase.MASHING)
                        .build();
            }
            val nextStepDto = StepDto.builder()
                    .stepNumber(nextStep.getStepNumber())
                    .percentage(nextStep.getPercentage())
                    .duration(nextStep.getTime())
                    .temperature(nextStep.getTemperature())
                    .build();

            return BrewResponseDto.builder()
                    .actualStep(actualStepDto)
                    .nextStep(nextStepDto)
                    .brewingPhase(BrewingPhase.MASHING)
                    .build();

        } else if (brewingSession.getBrewingPhase() == BrewingPhase.MASHING) {
            val currentStepNumber = brewingSession.getCurrentStep();
            val nextStep = recipe.getMashingSteps().stream()
                    .filter(step -> step.getStepNumber() == currentStepNumber + 1)
                    .findFirst()
                    .orElse(null);

            if (nextStep == null) {
                brewingSession.setBrewingPhase(BrewingPhase.BOILING);
                brewingSession.setCurrentStep(0);
                brewSessionRepository.save(brewingSession);

                return BrewResponseDto.builder()
                        .lautering(true)
                        .build();
            }

            brewingSession.setBrewingPhase(BrewingPhase.MASHING);
            brewingSession.setCurrentStep(currentStepNumber + 1);
            brewSessionRepository.save(brewingSession);

            var overPumping = true;
            val currentStep = recipe.getMashingSteps().stream()
                    .filter(step -> Objects.equals(step.getStepNumber(), currentStepNumber))
                    .findFirst()
                    .orElseThrow();

            var decoctionTemperature = 0;

            if (nextStep.getPercentage() == 100) {
                if (currentStep.getPercentage() == 100) {
                    overPumping = false;
                }
            } else {
                if (currentStep.getPercentage() != 100) {
                    var stop = true;
                    overPumping = false;
                    while (stop) {
                        val step = recipe.getMashingSteps().stream()
                                .filter(s -> Objects.equals(s.getStepNumber(), currentStepNumber - 1))
                                .findFirst()
                                .orElseThrow();
                        if (step.getPercentage() == 100) {
                            stop = false;
                            decoctionTemperature = step.getTemperature();
                        }
                    }
                } else {
                    decoctionTemperature = currentStep.getTemperature();
                }
            }

            val afterNextStep = recipe.getMashingSteps().stream()
                    .filter(step -> Objects.equals(step.getStepNumber(), currentStepNumber + 2))
                    .findFirst()
                    .orElse(null);

            val actualStepDto = StepDto.builder()
                    .stepNumber(nextStep.getStepNumber())
                    .percentage(nextStep.getPercentage())
                    .duration(nextStep.getTime())
                    .temperature(nextStep.getTemperature())
                    .build();

            if (afterNextStep == null) {
                return BrewResponseDto
                        .builder()
                        .brewingPhase(BrewingPhase.MASHING)
                        .decoctionTemperature(decoctionTemperature == 0 ? null : decoctionTemperature)
                        .actualStep(actualStepDto)
                        .overpumping(overPumping)
                        .build();
            }

            val afterNextStepDto = StepDto.builder()
                    .stepNumber(afterNextStep.getStepNumber())
                    .percentage(afterNextStep.getPercentage())
                    .duration(afterNextStep.getTime())
                    .temperature(afterNextStep.getTemperature())
                    .build();

            hardwareControlService.turnOnHeater(actualStepDto.getTemperature());

            return BrewResponseDto
                    .builder()
                    .brewingPhase(BrewingPhase.MASHING)
                    .decoctionTemperature(decoctionTemperature == 0 ? null : decoctionTemperature)
                    .actualStep(actualStepDto)
                    .nextStep(afterNextStepDto)
                    .overpumping(overPumping)
                    .build();
        } else if (brewingSession.getBrewingPhase() == BrewingPhase.BOILING) {
            val currentStepNumber = brewingSession.getCurrentStep();
            val nextStep = recipe.getHoppingSteps().stream()
                    .filter(hoppingStep -> Objects.equals(hoppingStep.getStepNumber(), currentStepNumber + 1))
                    .findFirst()
                    .orElse(null);

            if (nextStep == null) {
                hardwareControlService.turnOffHeater();

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

            hardwareControlService.turnOnHeater(100);
            hardwareControlService.turnOffMashMixing();

            val firstStep = recipe.getHoppingSteps().stream()
                    .filter(step -> Objects.equals(step.getStepNumber(), 1))
                    .findFirst()
                    .orElse(null);

            val afterNextStep = recipe.getHoppingSteps().stream()
                    .filter(step -> Objects.equals(step.getStepNumber(), currentStepNumber + 2))
                    .findFirst()
                    .orElse(null);

            val actualStepDto = StepDto.builder()
                    .stepNumber(nextStep.getStepNumber())
                    .duration(nextStep.getTime())
                    .name(nextStep.getName())
                    .build();

            if (afterNextStep == null) {
                assert firstStep != null;
                return BrewResponseDto.builder()
                        .heatingTemperature(100)
                        .brewingPhase(BrewingPhase.BOILING)
                        .boilingTime(firstStep.getTime())
                        .actualStep(actualStepDto)
                        .build();
            }

            val nextStepDto = StepDto.builder()
                    .stepNumber(afterNextStep.getStepNumber())
                    .duration(afterNextStep.getTime())
                    .name(afterNextStep.getName())
                    .build();

            assert firstStep != null;
            return BrewResponseDto.builder()
                    .heatingTemperature(100)
                    .brewingPhase(BrewingPhase.BOILING)
                    .boilingTime(firstStep.getTime())
                    .actualStep(actualStepDto)
                    .nextStep(nextStepDto)
                    .build();
        }
        return null;
    }

}
