package dev.aprokhorenko.ticked_classifier.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tickeds")
@Getter
@Setter
@NoArgsConstructor
public class Ticked {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private TickedCategory category;

    @Enumerated(EnumType.STRING)
    private TickedUrgency urgency;

    @Enumerated(EnumType.STRING)
    private TickedFeeling feeling;

    @Enumerated(EnumType.STRING)
    private TickedStatus status;

}
