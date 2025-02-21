package cz.diplomka.pivovar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mashing_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MashingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer stepNumber;

    @Column(nullable = false)
    private Integer temperature;

    @Column(name = "duration", nullable = false)
    private Integer time;

    @Column(nullable = false)
    private Integer percentage;
}

