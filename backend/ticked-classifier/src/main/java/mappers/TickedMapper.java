package mappers;

import org.springframework.stereotype.Component;

import dto.TickedResponseDTO;
import entities.Ticked;

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
