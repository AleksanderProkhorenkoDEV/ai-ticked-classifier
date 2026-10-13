package dev.aprokhorenko.ticked_classifier.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.aprokhorenko.ticked_classifier.dto.ArchivedTicketRequestDTO;
import dev.aprokhorenko.ticked_classifier.dto.CreateTicketRequestDTO;
import dev.aprokhorenko.ticked_classifier.dto.TicketClasification;
import dev.aprokhorenko.ticked_classifier.dto.TicketStatsDTO;
import dev.aprokhorenko.ticked_classifier.dto.UpdateStatusTicketDTO;
import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;
import dev.aprokhorenko.ticked_classifier.repositories.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TicketServices {

    private TicketRepository tickedRepository;
    private TicketClasificationServices ticketClasificationServices;

    public TicketServices(TicketRepository tickedRepository, TicketClasificationServices ticketClasificationServices) {
        this.tickedRepository = tickedRepository;
        this.ticketClasificationServices = ticketClasificationServices;
    }

    public Page<Ticket> getAllTickeds(int page, int size,
        TicketUrgency urgency, TicketCategory category, String search) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return tickedRepository.findFiltered(urgency, category, term, PageRequest.of(page, size));
    }

    public Ticket getTickedById(Long id) {
        Ticket ticked = tickedRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return ticked;
    }

    @Transactional
    public void createTicked(CreateTicketRequestDTO request) {
        TicketClasification classification = ticketClasificationServices.classifyTicket(request.getTitle(),
                request.getDescription());

        Ticket ticket = new Ticket(request.getTitle(), request.getDescription(), classification.category(),
                classification.urgency(), classification.feeling());

        tickedRepository.save(ticket);
    }

    @Transactional
    public void updateStatus(UpdateStatusTicketDTO request, Long id) {
        Ticket ticket = getTickedById(id);
        TicketStatus status = request.getStatus();
        ticket.changeStatus(status);
        ticket.changeArchived(TicketStatus.ARCHIVADO.equals(status));
    }

    @Transactional
    public void archivedTicket(ArchivedTicketRequestDTO request, Long id) {
        Ticket ticket = getTickedById(id);
        Boolean archived = request.getArchived();
        ticket.changeArchived(archived);
        // El estado cambia en la misma transaccion, para que archivar mueva tambien la
        // tarjeta de "open" (que cuenta por status) y no solo la de "resolved".
        ticket.changeStatus(archived ? TicketStatus.ARCHIVADO : TicketStatus.ABIERTO);
    }

    public TicketStatsDTO getTicketStats() {
        return new TicketStatsDTO(
                tickedRepository.count(),
                tickedRepository.countByStatus(TicketStatus.ABIERTO),
                tickedRepository.countByUrgency(TicketUrgency.ALTA),
                tickedRepository.countByArchivedTrue());
    }
}
