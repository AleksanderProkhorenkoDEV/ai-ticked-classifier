package dev.aprokhorenko.ticked_classifier.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aprokhorenko.ticked_classifier.dto.CreateTicketRequestDTO;
import dev.aprokhorenko.ticked_classifier.dto.GlobalResponseDTO;
import dev.aprokhorenko.ticked_classifier.dto.PageResponseDTO;
import dev.aprokhorenko.ticked_classifier.dto.TicketResponseDTO;
import dev.aprokhorenko.ticked_classifier.mappers.TicketMapper;
import dev.aprokhorenko.ticked_classifier.services.TickedServices;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/tickeds")
public class TicketController {

    private TickedServices tickedServices;
    private TicketMapper tickedMapper;

    public TicketController(TickedServices tickedServices, TicketMapper tickedMapper) {
        this.tickedServices = tickedServices;
        this.tickedMapper = tickedMapper;
    }

    @GetMapping()
    public ResponseEntity<PageResponseDTO<TicketResponseDTO>> getAllTicked(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size) {

        Page<TicketResponseDTO> dtoPage = tickedServices.getAllTickeds(page, size).map(tickedMapper::toDto);

        return ResponseEntity.ok(new PageResponseDTO<>(dtoPage));
    }

    @PostMapping("/create")
    public ResponseEntity<GlobalResponseDTO> createTicked(@Valid @RequestBody CreateTicketRequestDTO request) {
        tickedServices.createTicked(request);
        return ResponseEntity.ok(new GlobalResponseDTO("Ticked creado", HttpStatus.CREATED.value()));
    }

}
