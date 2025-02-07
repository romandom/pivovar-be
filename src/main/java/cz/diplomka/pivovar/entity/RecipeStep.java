package cz.diplomka.pivovar.entity;

import cz.diplomka.pivovar.constant.BrewingProcess;
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

@Data
@Entity
@Table(name = "recipe_steps")
public class RecipeStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int stepNumber;

    @Column(nullable = false)
    private double targetTemperature;

    @Column(nullable = false)
    private int duration;

    @Enumerated(EnumType.STRING)
    private BrewingVessel vessel;

    @Column(nullable = false)
    private boolean isDecoctionStep;

    @Enumerated(EnumType.STRING)
    private BrewingProcess process;

}

