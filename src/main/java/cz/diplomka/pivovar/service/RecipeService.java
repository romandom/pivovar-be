package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.constant.RecipeStatus;
import cz.diplomka.pivovar.dto.RecipeList;
import cz.diplomka.pivovar.model.Recipe;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;


    public Recipe createRecipe(Recipe recipeToSave) {
        return recipeRepository.save(recipeToSave);
    }


    public void deleteRecipeById(int id) {
        recipeRepository.deleteById(id);
    }

    public List<RecipeList> getAllRecipes() {
        val recipeList = recipeRepository.findAll();
        return recipeList.stream().map(recipe -> new RecipeList(
                        recipe.getId(),
                        recipe.getName(),
                        recipe.getStyle(),
                        recipe.getIbu(),
                        recipe.getAlcohol()
                ))
                .collect(Collectors.toList());
    }

    public Long getBrewedRecipe() {
        val recipeList = recipeRepository.findAll();
        val brewedRecipe = recipeList.stream().filter(r -> r.getStatus() == RecipeStatus.BREWING).findFirst().orElse(null);
        return brewedRecipe == null ? null : brewedRecipe.getId();
    }
}
