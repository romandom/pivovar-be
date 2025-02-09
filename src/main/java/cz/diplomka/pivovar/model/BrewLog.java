package cz.diplomka.pivovar.model;

import cz.diplomka.pivovar.constant.MeasurementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "brew_logs")
@Getter
@Setter
@NoArgsConstructor
public class BrewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vessel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeasurementType measurementType;

    private BigDecimal temperature;
    private BigDecimal weight;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}

