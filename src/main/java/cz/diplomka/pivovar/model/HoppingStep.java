package cz.diplomka.pivovar.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hopping_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoppingStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer stepNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false)
    private Integer time;
}

