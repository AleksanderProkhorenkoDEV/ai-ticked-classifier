package dev.aprokhorenko.ticked_classifier.services;

import org.springframework.stereotype.Service;

import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.repositories.TicketRepository;
import jakarta.transaction.Transactional;

/**
 * Separate bean on purpose: a call to a transactional method inside {@link TicketServices}
 * itself would bypass the Spring proxy and the transaction would never open. Isolating the write
 * also means the database connection is not held while the moderation and classification calls
 * are in flight.
 */
@Service
public class TicketPersistenceServices {

    private final TicketRepository ticketRepository;

    public TicketPersistenceServices(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public void saveNew(Ticket ticket) {
        ticketRepository.save(ticket);
    }
}