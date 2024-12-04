package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.dto.StepRequestDto;
import cz.diplomka.pivovar.entity.RecipeStep;
import cz.diplomka.pivovar.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StepService {

    private final RecipeRepository recipeRepository;

    public RecipeStep getStepByRecipeIdAndStepId(StepRequestDto stepRequestDto) {
        val recipe = recipeRepository.findById(stepRequestDto.getRecipeId()).orElseThrow();
        return recipe.getSteps()
                .stream()
                .filter(step -> step.getStepNumber() == stepRequestDto.getStepNumber())
                .toList()
                .getFirst();
    }

}
