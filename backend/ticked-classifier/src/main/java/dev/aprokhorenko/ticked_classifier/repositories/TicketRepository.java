package dev.aprokhorenko.ticked_classifier.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.aprokhorenko.ticked_classifier.entities.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
