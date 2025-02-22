package cz.diplomka.pivovar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StepDto {
    private Integer stepNumber;
    private String name;
    private Integer temperature;
    private Integer duration;
    private Integer percentage;
    private Integer weight;
}
