package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.arduino.HardwareControlService;
import cz.diplomka.pivovar.constant.BrewingPhase;
import cz.diplomka.pivovar.constant.BrewingStatus;
import cz.diplomka.pivovar.dto.BrewResponse;
import cz.diplomka.pivovar.dto.StartBrewResponse;
import cz.diplomka.pivovar.model.BrewSession;
import cz.diplomka.pivovar.repository.BrewSessionRepository;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class BrewService {

    private final RecipeRepository recipeRepository;
    private final BrewSessionRepository brewSessionRepository;

    private final HardwareControlService hardwareControlService;

    public StartBrewResponse startBrewing(int recipeId) throws IOException {
        val recipe = recipeRepository.findById(recipeId).orElseThrow();
        val brewSession = createNewBrewSession();

        recipe.getBrewSessions().add(brewSession);
        recipeRepository.save(recipe);

        val firstMashingStep = recipe.getMashingSteps()
                .stream()
                .filter(mashingStep -> mashingStep.getStepNumber() == 1)
                .findFirst()
                .orElseThrow();

        hardwareControlService.turnOnHeater(firstMashingStep.getTemperature());

        return new StartBrewResponse(
                "Ohrev",
                firstMashingStep.getTemperature());
    }

    public String doughing(int recipeId) throws IOException {
        val recipe = recipeRepository.findById(recipeId).orElseThrow();
        val brewSession = recipe.getBrewSessions()
                .stream()
                .filter(bs -> bs.getStatus().equals(BrewingStatus.IN_PROGRESS))
                .findFirst()
                .orElseThrow();

        brewSession.setBrewingPhase(BrewingPhase.DOUGHING);

        brewSessionRepository.save(brewSession);

        hardwareControlService.turnOnMashMixing();

        return "Nasypte " + recipe.getIngredient().getMalts().stream()
                .map(malt -> malt.getName() + " - " + malt.getWeight() + " kg")
                .collect(Collectors.joining(", "));
    }

    public BrewResponse nextBrewingStep(int recipeId) {
        val recipe = recipeRepository.findById(recipeId).orElseThrow();
        return null;
    }

    private BrewSession createNewBrewSession() {
        val session = new BrewSession();
        session.setStartTime(LocalDateTime.now());
        session.setStatus(BrewingStatus.IN_PROGRESS);
        session.setBrewingPhase(BrewingPhase.HEATING);
        return session;
    }


}
