package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.BrewResponseDto;
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

    @PostMapping("/next-step/{recipeId}")
    public ResponseEntity<BrewResponseDto> nextBrewingStep(@PathVariable("recipeId") int recipeId) throws IOException {
        return ResponseEntity.ok(brewService.nextBrewingStep(recipeId));
    }
}
