package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.BrewResponse;
import cz.diplomka.pivovar.dto.StartBrewResponse;
import cz.diplomka.pivovar.service.BrewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/brew")
public class BrewController {

    private final BrewService brewService;

    @PostMapping("/start/{recipeId}")
    public ResponseEntity<StartBrewResponse> startBrewing(@PathVariable("recipeId") int recipeId) throws IOException {
        return ResponseEntity.ok(brewService.startBrewing(recipeId));
    }

    @PostMapping("/doughing/{recipeId}")
    public ResponseEntity<String> doughing(@PathVariable("recipeId") int recipeId) throws IOException {
        return ResponseEntity.ok(brewService.doughing(recipeId));
    }

    @PostMapping("/next-step/{recipeId}")
    public ResponseEntity<BrewResponse> nextBrewingStep(@PathVariable("recipeId") int recipeId) throws IOException {
        return ResponseEntity.ok(brewService.nextBrewingStep(recipeId));
    }
}
