package cz.diplomka.pivovar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mashing_steps")
@Getter
@Setter
@NoArgsConstructor
public class MashingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer stepNumber;

    @Column(name = "duration", nullable = false)
    private Integer time;

    @Column(nullable = false)
    private Integer percentage;
}

