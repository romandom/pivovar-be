package cz.diplomka.pivovar.service;

import cz.diplomka.pivovar.dto.RecipeListDto;
import cz.diplomka.pivovar.model.Recipe;
import cz.diplomka.pivovar.repository.RecipeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;


    public Recipe createRecipe(Recipe recipeToSave) {
        log.debug("Creating new recipe");
        return recipeRepository.save(recipeToSave);
    }


    public void deleteRecipeById(int id) {
        log.debug("Deleting recipe with id {}", id);
        recipeRepository.deleteById(id);
    }

    public List<RecipeListDto> getAllRecipes() {
        val recipeList = recipeRepository.findAll();
        log.debug("Get all recipes");
        return recipeList.stream().map(recipe -> new RecipeListDto(
                        recipe.getId(),
                        recipe.getName(),
                        recipe.getStyle(),
                        recipe.getIbu(),
                        recipe.getAlcohol()
                ))
                .collect(Collectors.toList());
    }
}
