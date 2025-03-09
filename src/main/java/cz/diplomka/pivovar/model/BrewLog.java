package cz.diplomka.pivovar.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "brew_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double mashTemperature;
    private double mashWeight;
    private double worthTemperature;
    private double worthWeight;


    @Column(nullable = false)
    private LocalDateTime timestamp;
}

