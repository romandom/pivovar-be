package cz.diplomka.pivovar.dto;

import cz.diplomka.pivovar.constant.BrewingPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrewResponseDto {
    private Integer heatingTemperature;
    private Integer decoctionTemperature;
    private Boolean lautering;
    private Boolean overpumping;
    private Boolean cooling;
    private Integer boilingTime;
    private BrewingPhase brewingPhase;
    private List<DoughingDto> doughingDtoList;
    private StepDto actualStep;
    private StepDto nextStep;
}
