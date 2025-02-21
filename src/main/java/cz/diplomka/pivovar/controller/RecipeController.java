package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.RecipeListDto;
import cz.diplomka.pivovar.model.Recipe;
import cz.diplomka.pivovar.repository.RecipeRepository;
import cz.diplomka.pivovar.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("recipe")
public class RecipeController {

    private final RecipeService recipeService;

    private final RecipeRepository recipeRepository;

    @PostMapping("create")
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        return ResponseEntity.ok(recipeService.createRecipe(recipe));
    }

    @GetMapping("list")
    public ResponseEntity<List<RecipeListDto>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable int id) {
        return ResponseEntity.ok(recipeRepository.findById(id).orElseThrow());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable int id) {
        recipeService.deleteRecipeById(id);
        return ResponseEntity.noContent().build();
    }
}
