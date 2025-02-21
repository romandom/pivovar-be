package cz.diplomka.pivovar.model;

import cz.diplomka.pivovar.constant.BrewingPhase;
import cz.diplomka.pivovar.constant.BrewingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "brew_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "brew_session_id")
    private List<BrewLog> brewLogs;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrewingStatus status;

    private Integer currentStep;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrewingPhase brewingPhase;
}

