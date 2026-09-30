package dev.aprokhorenko.ticked_classifier.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.aprokhorenko.ticked_classifier.dto.ArchivedTicketRequestDTO;
import dev.aprokhorenko.ticked_classifier.dto.CreateTicketRequestDTO;
import dev.aprokhorenko.ticked_classifier.dto.TicketClasification;
import dev.aprokhorenko.ticked_classifier.dto.UpdateStatusTicketDTO;
import dev.aprokhorenko.ticked_classifier.entities.Ticket;
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

    public Page<Ticket> getAllTickeds(Integer page, Integer sizePerPage) {
        Pageable pageable = PageRequest.of(page, sizePerPage);
        return tickedRepository.findAll(pageable);
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
        ticket.changeStatus(request.getStatus());
    }

    @Transactional
    public void archivedTicket(ArchivedTicketRequestDTO request, Long id) {
        Ticket ticket = getTickedById(id);
        ticket.changeArchived(request.getArchived());
    }
}
