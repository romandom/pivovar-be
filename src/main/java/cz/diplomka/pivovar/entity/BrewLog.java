package cz.diplomka.pivovar.entity;

import cz.diplomka.pivovar.constant.BrewingVessel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "brew_logs")
public class BrewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BrewingVessel vessel;

    @Column(nullable = false)
    private double temperature;

    @Column(nullable = false)
    private LocalDateTime timestamp;

}

