package cz.diplomka.pivovar.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal weight;
}

