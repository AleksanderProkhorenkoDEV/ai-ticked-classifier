package dev.aprokhorenko.ticked_classifier.dto;

import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusTicketDTO {

    @NotNull(message = "{validation.notNull}")
    private TicketStatus status;
}