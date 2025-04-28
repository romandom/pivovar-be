package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.constant.BrewingPhase;
import cz.diplomka.pivovar.constant.BrewingStatus;
import cz.diplomka.pivovar.dto.*;
import cz.diplomka.pivovar.model.*;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class BrewService {

    private final RecipeRepository recipeRepository;
    private final BrewSessionRepository brewSessionRepository;

    public BrewResponseDto nextBrewingStep(int recipeId) {
        var optionalRecipe = recipeRepository.findById(recipeId);

        if (optionalRecipe.isEmpty()) {
            log.error("Entity not found for recipe {}", recipeId);
        }

        var recipe = optionalRecipe.get();

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
                        log.error("Error processing brewing step", e);
                    }
                    return null;
                })
                .orElseGet(() -> {
                    try {
                        return handleNullBrewingSession(recipe);
                    } catch (IOException | InterruptedException e) {
                        log.error("Error handling null brewing session", e);
                    }
                    return null;
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
        log.debug("Brewing session {} started", brewingSession.getId());
        recipeRepository.save(recipe);

        final int heatingTemperature = getMashingStepByStepNumber(recipe, 1).getTemperature();

        final List<DoughingDto> doughingDtoList = recipe.getIngredient().getMalts()
                .stream()
                .map(malt -> DoughingDto.builder().name(malt.getName()).weight(malt.getWeight()).build())
                .toList();

        return BrewResponseDto.builder()
                .heatingTemperature(heatingTemperature)
                .doughingDtoList(doughingDtoList)
                .build();
    }

    private BrewResponseDto handleStartedPhase(Recipe recipe, BrewSession brewingSession) throws IOException, InterruptedException {
        brewingSession.setCurrentStep(1);
        brewingSession.setBrewingPhase(BrewingPhase.MASHING);
        brewSessionRepository.save(brewingSession);

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

            brewingSession.setBrewingPhase(BrewingPhase.BOILING);
            brewingSession.setCurrentStep(0);
            brewSessionRepository.save(brewingSession);

            return BrewResponseDto.builder()
                    .cooling(true)
                    .build();
        }

        brewingSession.setBrewingPhase(BrewingPhase.BOILING);
        brewingSession.setCurrentStep(currentStepNumber + 1);
        brewSessionRepository.save(brewingSession);

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

        if (isOlderThanTwelveHours(brewSession)) {
            brewSession.setEndTime(LocalDateTime.now());
            brewSession.setStatus(BrewingStatus.CANCELLED);
            brewSessionRepository.save(brewSession);
            return false;
        }
        return true;
    }

    private boolean isOlderThanTwelveHours(BrewSession brewSession) {
        LocalDateTime twelveHoursAgo = LocalDateTime.now().minusHours(12);
        return brewSession.getStartTime().isBefore(twelveHoursAgo);
    }

    public List<TemperatureGraphDto> getTemperaturesToGraph() {
        var brewSession = brewSessionRepository.findBrewSessionByStatus(BrewingStatus.IN_PROGRESS).getFirst();

        return brewSession.getBrewLogs()
                .stream()
                .sorted(Comparator.comparing(BrewLog::getTimestamp))
                .map(brewLog -> {
                    final long minutes = Duration.between(brewSession.getStartTime(), brewLog.getTimestamp()).toMinutes();
                    return new TemperatureGraphDto(minutes, brewLog.getMashTemperature(), brewLog.getWorthTemperature());
                }).toList();
    }

    public List<WeightGraphDto> getWeightsToGraph() {
        var brewSession = brewSessionRepository.findBrewSessionByStatus(BrewingStatus.IN_PROGRESS).getFirst();

        return brewSession.getBrewLogs()
                .stream()
                .sorted(Comparator.comparing(BrewLog::getTimestamp))
                .map(brewLog -> {
                    final long minutes = Duration.between(brewSession.getStartTime(), brewLog.getTimestamp()).toMinutes();
                    return new WeightGraphDto(minutes, brewLog.getMashWeight());
                }).toList();
    }

    public List<PowerGraphDto> getPowerToGraph() {
        var brewSession = brewSessionRepository.findBrewSessionByStatus(BrewingStatus.IN_PROGRESS).getFirst();

        return brewSession.getBrewLogs()
                .stream()
                .sorted(Comparator.comparing(BrewLog::getTimestamp))
                .map(brewLog -> {
                    final long minutes = Duration.between(brewSession.getStartTime(), brewLog.getTimestamp()).toMinutes();
                    return new PowerGraphDto(minutes, brewLog.getPower());
                }).toList();
    }

}
