package cz.diplomka.pivovar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryGraphDataDto {
    long minute;
    double mashTemperature;
    double worthTemperature;
    double power;
    int mashWeight;
    int worthHeight;
}
