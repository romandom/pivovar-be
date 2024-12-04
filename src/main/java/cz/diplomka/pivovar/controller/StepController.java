package cz.diplomka.pivovar.controller;

import cz.diplomka.pivovar.dto.StepRequestDto;
import cz.diplomka.pivovar.entity.RecipeStep;
import cz.diplomka.pivovar.service.StepService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("step")
public class StepController {

    private final StepService stepService;

    @GetMapping
    public ResponseEntity<RecipeStep> getStep(@RequestBody StepRequestDto stepRequestDto) {
        return ResponseEntity.ok(stepService.getStepByRecipeIdAndStepId(stepRequestDto));
    }
}
