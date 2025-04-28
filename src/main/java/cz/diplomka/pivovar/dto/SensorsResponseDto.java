package cz.diplomka.pivovar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SensorsResponseDto {
    private double mashTemperature;
    private double worthTemperature;
    private int mashWeight;
    private double worthHeight;
    private boolean mixing;
    private boolean heating;
    private double power;
}
