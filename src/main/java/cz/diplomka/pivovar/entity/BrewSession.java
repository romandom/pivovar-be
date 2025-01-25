package cz.diplomka.pivovar.entity;

import cz.diplomka.pivovar.constant.BrewSessionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "brew_sessions")
public class BrewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private BrewSessionStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "brew_session_id")
    private List<BrewLog> logs;

    @Column
    private Integer currentStep;

}

