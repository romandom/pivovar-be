package cz.diplomka.pivovar.model;

import cz.diplomka.pivovar.constant.MashType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "recipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String style;

    private Integer wort;
    private Integer alcohol;
    private Integer ibu;
    private Integer ebc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MashType mashType;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<MashingStep> mashingSteps;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<HoppingStep> hoppingSteps;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ingredient_id", unique = true)
    private Ingredient ingredient;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recipe_id")
    private List<BrewSession> brewSessions;
}

