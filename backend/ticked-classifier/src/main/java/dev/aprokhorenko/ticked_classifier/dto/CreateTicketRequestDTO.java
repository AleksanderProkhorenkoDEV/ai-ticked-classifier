package dev.aprokhorenko.ticked_classifier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTicketRequestDTO {
    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 120, message = "{validation.size.title}")
    private String title;

    @NotBlank(message = "{validation.notBlank}")
    @Size(max = 255, message = "{validation.size.description}")
    private String description;
}