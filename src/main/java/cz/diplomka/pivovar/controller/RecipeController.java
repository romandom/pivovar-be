package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.entity.Recipe;
import cz.diplomka.pivovar.repository.RecipeRepository;
import cz.diplomka.pivovar.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable int id) {
        recipeService.deleteRecipeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("names")
    public ResponseEntity<Map<Long, String>> getAllRecipesNames() {
        return ResponseEntity.ok(recipeService.getAllRecipeNames());
    }

    @GetMapping("{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable int id) {
        return ResponseEntity.ok(recipeRepository.findById(id).orElse(null));
    }
}
