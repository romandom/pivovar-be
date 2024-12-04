package cz.diplomka.pivovar.dto;

import lombok.Data;

@Data
public class StepRequestDto {
    private int recipeId;
    private int stepNumber;
}
