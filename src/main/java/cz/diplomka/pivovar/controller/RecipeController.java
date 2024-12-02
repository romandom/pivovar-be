package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.entity.Recipe;
import cz.diplomka.pivovar.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("recipe")
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping("create")
    public ResponseEntity<Recipe> createRecipe(@RequestBody Recipe recipe) {
        return ResponseEntity.ok(recipeService.createRecipe(recipe));
    }

    @GetMapping("names")
    public ResponseEntity<Map<Long, String>> getAllRecipesNames() {
        return ResponseEntity.ok(recipeService.getAllRecipeNames());
    }
}
