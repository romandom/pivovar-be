//package cz.diplomka.pivovar.service;
//
//import cz.diplomka.pivovar.dto.StepResponse;
//import cz.diplomka.pivovar.repository.RecipeRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.val;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@RequiredArgsConstructor
//@Service
//@Transactional
//public class StepService {
//
//    private final RecipeRepository recipeRepository;
//
//    private final BrewSessionRepository brewSessionRepository;
//
//    public StepResponse getStep(int recipeId) {
//        val recipe = recipeRepository.findById(recipeId).orElseThrow();
//
//        val currentStepNumber = checkIfRecipeStarted(recipe);
//
//        val optionalCurrentStep = getCurrentStepFromRecipe(recipe, currentStepNumber);
//
//        if (optionalCurrentStep.isEmpty()) {
//            setBrewLogEndTime(recipe, currentStepNumber);
//            return new StepResponse(0,false,0,0);
//        }
//
//        val currentStep = optionalCurrentStep.get();
//
//        val isDecoction = checkIfCurrentStepIsDecoction(currentStep);
//
//        var mashTemperature = 0.0;
//        var worthTemperature = 0.0;
//
//        if (isDecoction) {
//            mashTemperature = getTemperatureOfRecentStep(recipe, currentStepNumber);
//            worthTemperature = currentStep.getTargetTemperature();
//        } else {
//            mashTemperature = currentStep.getTargetTemperature();
//        }
//
//        val targetTimeInSeconds = convertTargetTimeToSeconds(currentStep.getDuration());
//
//        return new StepResponse(targetTimeInSeconds, true, mashTemperature, worthTemperature);
//    }
//
//    private void setBrewLogEndTime(Recipe recipe, int stepNumber) {
//        val recentStepNumber = stepNumber - 1;
//        val session = recipe.getSessions()
//                .stream()
//                .filter(s -> s.getCurrentStep() == recentStepNumber)
//                .findFirst()
//                .orElseThrow();
//        session.setEndTime(LocalDateTime.now());
//        session.setStatus(BrewSessionStatus.COMPLETED);
//        brewSessionRepository.save(session);
//    }
//
//    private int convertTargetTimeToSeconds(int minutes) {
//        return minutes * 60;
//    }
//
//    private double getTemperatureOfRecentStep(Recipe recipe, int currentStepNumber) {
//        var stepNumberToCheck = currentStepNumber - 1;
//
//        while (stepNumberToCheck > 0) {
//            val finalStepNumberToCheck = stepNumberToCheck;
//            RecipeStep step = recipe.getSteps()
//                    .stream()
//                    .filter(s -> s.getStepNumber() == finalStepNumberToCheck)
//                    .findFirst()
//                    .orElseThrow(() -> new IllegalStateException("No step found for stepNumber: " + finalStepNumberToCheck));
//
//            if (!step.isDecoctionStep()) {
//                return step.getTargetTemperature();
//            }
//            stepNumberToCheck--;
//        }
//
//        throw new IllegalStateException("No valid step found before step " + currentStepNumber);
//    }
//
//
//    private boolean checkIfCurrentStepIsDecoction(RecipeStep currentStep) {
//        return currentStep.isDecoctionStep();
//    }
//
//    private void createBrewSession(Recipe recipe) {
//        val brewSession = new BrewSession();
//        brewSession.setCurrentStep(1);
//        brewSession.setStartTime(LocalDateTime.now());
//        brewSession.setStatus(BrewSessionStatus.IN_PROGRESS);
//
//        recipe.getSessions().add(brewSession);
//        recipeRepository.save(recipe);
//    }
//
//    private int checkIfRecipeStarted(Recipe recipe) {
//        val sessions = recipe.getSessions();
//
//        val session = sessions.stream()
//                .filter(bs -> bs.getStatus() == BrewSessionStatus.IN_PROGRESS)
//                .findFirst();
//
//        var currentStepNumber = 1;
//
//        if (session.isPresent()) {
//            currentStepNumber = session.get().getCurrentStep();
//            currentStepNumber++;
//            if (getCurrentStepFromRecipe(recipe,currentStepNumber).isPresent()) {
//                session.get().setCurrentStep(currentStepNumber);
//                brewSessionRepository.save(session.get());
//            }
//        } else {
//            createBrewSession(recipe);
//        }
//
//        return currentStepNumber;
//    }
//
//    private Optional<RecipeStep> getCurrentStepFromRecipe(Recipe recipe, int currentStepNumber) {
//        return recipe.getSteps()
//                .stream()
//                .filter(recipeStep -> recipeStep.getStepNumber() == currentStepNumber)
//                .findFirst();
//    }
//}
