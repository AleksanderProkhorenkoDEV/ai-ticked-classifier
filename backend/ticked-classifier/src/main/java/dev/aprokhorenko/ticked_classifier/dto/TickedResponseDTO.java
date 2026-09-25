package dev.aprokhorenko.ticked_classifier.dto;

import java.time.LocalDateTime;

import dev.aprokhorenko.ticked_classifier.entities.TickedCategory;
import dev.aprokhorenko.ticked_classifier.entities.TickedFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TickedStatus;
import dev.aprokhorenko.ticked_classifier.entities.TickedUrgency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TickedResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;
    private TickedCategory category;
    private TickedUrgency urgency;
    private TickedFeeling feeling;
    private TickedStatus status;
}
