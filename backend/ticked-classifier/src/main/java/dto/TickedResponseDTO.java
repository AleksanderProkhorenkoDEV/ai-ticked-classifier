package dto;

import java.time.LocalDateTime;

import entities.TickedCategory;
import entities.TickedFeeling;
import entities.TickedStatus;
import entities.TickedUrgency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TickedResponseDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime date;
    private TickedCategory category;
    private TickedUrgency urgency;
    private TickedFeeling feeling;
    private TickedStatus status;
}
