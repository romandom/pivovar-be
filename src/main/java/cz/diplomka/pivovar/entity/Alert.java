package cz.diplomka.pivovar.entity;

import cz.diplomka.pivovar.constant.AlertType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brew_session_id", nullable = false)
    private BrewSession brewSession;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    private AlertType type; // Enum: TEMPERATURE, ACTION, ERROR

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean resolved;

    @Column
    private LocalDateTime resolvedAt;

}

