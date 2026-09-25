package dev.aprokhorenko.ticked_classifier.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateTickedRequestDTO {
    @NotEmpty(message = "{validation.notEmpty}")
    private String title;
    @NotEmpty(message = "{validation.notEmpty}")
    private String description;
}
