package cz.diplomka.pivovar.entity;

import cz.diplomka.pivovar.constant.BrewSessionStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @OneToMany(mappedBy = "brewSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BrewLog> logs;

    @Column
    private Integer currentStep;

}

