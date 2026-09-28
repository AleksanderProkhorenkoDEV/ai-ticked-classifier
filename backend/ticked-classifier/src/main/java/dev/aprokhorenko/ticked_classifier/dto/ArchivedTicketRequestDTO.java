package dev.aprokhorenko.ticked_classifier.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArchivedTicketRequestDTO {

    @NotNull(message = "{validation.notNull}")
    private Boolean archived;
}