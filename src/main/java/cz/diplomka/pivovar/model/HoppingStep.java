package cz.diplomka.pivovar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hopping_steps")
@Getter
@Setter
@NoArgsConstructor
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

