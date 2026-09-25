package dev.aprokhorenko.ticked_classifier.mappers;

import org.springframework.stereotype.Component;

import dev.aprokhorenko.ticked_classifier.dto.TickedResponseDTO;
import dev.aprokhorenko.ticked_classifier.entities.Ticked;

@Component
public class TickedMapper {

    public TickedResponseDTO toDto(Ticked ticked) {
        return new TickedResponseDTO(
                ticked.getId(),
                ticked.getTitle(),
                ticked.getDescription(),
                ticked.getDate(),
                ticked.getCategory(),
                ticked.getUrgency(),
                ticked.getFeeling(),
                ticked.getStatus());
    }
}
