package dev.aprokhorenko.ticked_classifier.services;

import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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

    private static final Pattern INVISIBLE_CHARS = Pattern.compile("[\\p{Cc}\\p{Cf}\\p{Z}]");
    private static final Pattern REPEATED_SPACES = Pattern.compile(" +");

    private TicketRepository ticketRepository;
    private TicketClasificationServices ticketClasificationServices;
    private TicketModerationServices ticketModerationServices;
    private TicketPersistenceServices ticketPersistenceServices;

    public TicketServices(TicketRepository ticketRepository,
            TicketClasificationServices ticketClasificationServices,
            TicketModerationServices ticketModerationServices,
            TicketPersistenceServices ticketPersistenceServices) {
        this.ticketRepository = ticketRepository;
        this.ticketClasificationServices = ticketClasificationServices;
        this.ticketModerationServices = ticketModerationServices;
        this.ticketPersistenceServices = ticketPersistenceServices;
    }

    public Page<Ticket> getAllTickeds(int page, int size,
        TicketUrgency urgency, TicketCategory category, String search) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return ticketRepository.findFiltered(urgency, category, term, PageRequest.of(page, size));
    }

    public Ticket getTickedById(Long id) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return ticket;
    }

    public void createTicked(CreateTicketRequestDTO request) {
        String title = normalize(request.getTitle());
        String description = normalize(request.getDescription());

        ticketModerationServices.moderate(title, description);

        TicketClasification classification = ticketClasificationServices.classifyTicket(title, description);

        Ticket ticket = new Ticket(title, description, classification.category(),
                classification.urgency(), classification.feeling());

        ticketPersistenceServices.saveNew(ticket);
    }

    @Transactional
    public void updateStatus(UpdateStatusTicketDTO request, Long id) {
        Ticket ticket = getTickedById(id);
        TicketStatus status = request.getStatus();
        ticket.changeStatus(status);
        ticket.changeArchived(TicketStatus.ARCHIVADO.equals(status));
    }

    public TicketStatsDTO getTicketStats() {
        return new TicketStatsDTO(
                ticketRepository.count(),
                ticketRepository.countByStatus(TicketStatus.ABIERTO),
                ticketRepository.countByUrgency(TicketUrgency.ALTA),
                ticketRepository.countByArchivedTrue());
    }

    /**
     * Trims, turns every invisible or exotic space into a plain one and collapses whitespace runs.
     * Besides keeping the stored text tidy it removes the invisible characters used to smuggle
     * instructions into the prompt. Validation already rejected blank input, so the result is
     * never empty.
     */
    private static String normalize(String value) {
        String withoutInvisibleChars = INVISIBLE_CHARS.matcher(value).replaceAll(" ");
        return REPEATED_SPACES.matcher(withoutInvisibleChars.trim()).replaceAll(" ");
    }
}