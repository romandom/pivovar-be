package cz.diplomka.pivovar.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DoughingDto {
    private String name;
    private BigDecimal weight;
}
