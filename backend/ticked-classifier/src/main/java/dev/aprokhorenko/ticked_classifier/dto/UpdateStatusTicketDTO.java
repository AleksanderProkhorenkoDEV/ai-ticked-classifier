package dev.aprokhorenko.ticked_classifier.dto;

import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import lombok.Data;

@Data
public class UpdateStatusTicketDTO {

    private TicketStatus status;
}
