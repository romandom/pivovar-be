package cz.diplomka.pivovar.dto;

import cz.diplomka.pivovar.constant.BrewingPhase;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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
