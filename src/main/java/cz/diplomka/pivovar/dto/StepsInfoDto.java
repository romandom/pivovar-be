package cz.diplomka.pivovar.dto;

import lombok.Data;

@Data
public class StepsInfoDto {
    private StepDto actualStep;
    private StepDto nextStep;
}
