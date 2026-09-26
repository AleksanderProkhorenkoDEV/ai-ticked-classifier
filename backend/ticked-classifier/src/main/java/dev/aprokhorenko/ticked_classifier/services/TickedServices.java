package dev.aprokhorenko.ticked_classifier.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.aprokhorenko.ticked_classifier.dto.CreateTicketRequestDTO;
import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.repositories.TicketRepository;
import jakarta.transaction.Transactional;

@Service
public class TickedServices {

    private TicketRepository tickedRepository;

    public TickedServices(TicketRepository tickedRepository) {
        this.tickedRepository = tickedRepository;
    }

    public Page<Ticket> getAllTickeds(Integer page, Integer sizePerPage) {
        Pageable pageable = PageRequest.of(page, sizePerPage);
        return tickedRepository.findAll(pageable);
    }

    @Transactional 
    public void createTicked(CreateTicketRequestDTO request) {
        
    }
}
