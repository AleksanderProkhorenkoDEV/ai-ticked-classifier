package dev.aprokhorenko.ticked_classifier.mappers;

import org.springframework.stereotype.Component;

import dev.aprokhorenko.ticked_classifier.dto.TicketResponseDTO;
import dev.aprokhorenko.ticked_classifier.entities.Ticket;

@Component
public class TicketMapper {

    public TicketResponseDTO toDto(Ticket ticked) {
        return new TicketResponseDTO(
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
