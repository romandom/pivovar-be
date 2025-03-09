package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.BrewResponseDto;
import cz.diplomka.pivovar.service.BrewService;
import cz.diplomka.pivovar.service.SekvenceService;
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
    private final SekvenceService sekvenceService;

    @PostMapping("/next-step/{recipeId}")
    public ResponseEntity<BrewResponseDto> nextBrewingStep(@PathVariable("recipeId") int recipeId) {
        return ResponseEntity.ok(brewService.nextBrewingStep(recipeId));
    }

    @PostMapping("/check/{recipeId}")
    public ResponseEntity<Boolean> checkBrewing(@PathVariable("recipeId") int recipeId) throws IOException {
        return ResponseEntity.ok(brewService.checkBrewing(recipeId));
    }

    @PostMapping("/start/{recipeId}")
    public ResponseEntity<Void> startBrewing(@PathVariable("recipeId") int recipeId) throws IOException {
        sekvenceService.startBrewing(recipeId);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/stop/{recipeId}")
//    public ResponseEntity<Boolean> stopBrewing(@PathVariable("recipeId") int recipeId) throws IOException {
//        return ResponseEntity.ok(brewService.checkBrewing(recipeId));
//    }

    @PostMapping("/doughing")
    public ResponseEntity<Void> doughing() {
        sekvenceService.doughingDone();
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/overpumping")
    public ResponseEntity<Void> overpumping() {
        sekvenceService.overpumpingDone();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lautering")
    public ResponseEntity<Void> lautering() {
        sekvenceService.lauteringDone();
        return ResponseEntity.noContent().build();
    }
}
