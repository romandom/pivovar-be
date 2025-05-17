package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.BrewResponseDto;
import cz.diplomka.pivovar.dto.PowerGraphDto;
import cz.diplomka.pivovar.dto.TemperatureGraphDto;
import cz.diplomka.pivovar.dto.WeightGraphDto;
import cz.diplomka.pivovar.service.BrewService;
import cz.diplomka.pivovar.service.SekvenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/start/{recipeId}")
    public ResponseEntity<Void> startBrewing(@PathVariable("recipeId") int recipeId) {
        sekvenceService.startBrewing(recipeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stopBrewing() {
        sekvenceService.stop();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/doughing")
    public ResponseEntity<Void> doughing() {
        sekvenceService.doughingDone();
        return ResponseEntity.noContent().build();
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

    @GetMapping("/graph/temperatures")
    public ResponseEntity<List<TemperatureGraphDto>> getTemperaturesToGraph() {
        return ResponseEntity.ok(brewService.getTemperaturesToGraph());
    }

    @GetMapping("/graph/weights")
    public ResponseEntity<List<WeightGraphDto>> getWeightsToGraph() {
        return ResponseEntity.ok(brewService.getWeightsToGraph());
    }

    @GetMapping("/graph/power")
    public ResponseEntity<List<PowerGraphDto>> getPowerToGraph() {
        return ResponseEntity.ok(brewService.getPowerToGraph());
    }
}
