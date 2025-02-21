package cz.diplomka.pivovar.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepDto {
    private Integer stepNumber;
    private String name;
    private Integer temperature;
    private Integer duration;
    private Integer percentage;
    private Integer weight;
}
