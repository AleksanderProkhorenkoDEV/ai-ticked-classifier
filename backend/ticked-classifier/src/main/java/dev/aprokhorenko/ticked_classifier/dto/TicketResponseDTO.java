package dev.aprokhorenko.ticked_classifier.dto;

import java.time.LocalDateTime;

import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TicketResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;
    private TicketCategory category;
    private TicketUrgency urgency;
    private TicketFeeling feeling;
    private TicketStatus status;
}
