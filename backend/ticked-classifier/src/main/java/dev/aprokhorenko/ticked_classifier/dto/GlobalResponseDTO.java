package dev.aprokhorenko.ticked_classifier.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GlobalResponseDTO {

    private String message;
    private Integer status;

}
