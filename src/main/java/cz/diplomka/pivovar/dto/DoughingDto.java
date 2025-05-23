package cz.diplomka.pivovar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoughingDto {
    private String name;
    private BigDecimal weight;

    @Override
    public String toString() {
        return name + ": " + weight + "kg";
    }
}
