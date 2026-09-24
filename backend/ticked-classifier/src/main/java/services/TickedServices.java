package services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import entities.Ticked;
import repositories.TickedRepository;

@Service
public class TickedServices {

    private TickedRepository tickedRepository;

    public TickedServices(TickedRepository tickedRepository) {
        this.tickedRepository = tickedRepository;
    }

    public Page<Ticked> getAllTickeds(Integer page, Integer sizePerPage) {
        Pageable pageable = PageRequest.of(page, sizePerPage);
        return tickedRepository.findAll(pageable);
    }
}
