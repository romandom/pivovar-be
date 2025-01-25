package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.entity.Recipe;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public Map<Long, String> getAllRecipeNames() {
        val recipes = recipeRepository.findAll();
        return recipes.stream().collect(Collectors.toMap(Recipe::getId, Recipe::getName));
    }


    public Recipe createRecipe(Recipe recipeToSave) {
        return recipeRepository.save(recipeToSave);
    }


    public void deleteRecipeById(int id) { recipeRepository.deleteById(id); }
}
