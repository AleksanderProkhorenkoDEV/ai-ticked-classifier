package dev.aprokhorenko.ticked_classifier.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByStatus(TicketStatus status);

    long countByUrgency(TicketUrgency urgency);

    long countByArchivedTrue();
}
