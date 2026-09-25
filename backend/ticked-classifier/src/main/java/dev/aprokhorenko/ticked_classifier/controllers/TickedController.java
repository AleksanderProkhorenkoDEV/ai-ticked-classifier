package dev.aprokhorenko.ticked_classifier.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aprokhorenko.ticked_classifier.dto.CreateTickedRequestDTO;
import dev.aprokhorenko.ticked_classifier.dto.GlobalResponseDTO;
import dev.aprokhorenko.ticked_classifier.dto.PageResponseDTO;
import dev.aprokhorenko.ticked_classifier.dto.TickedResponseDTO;
import dev.aprokhorenko.ticked_classifier.mappers.TickedMapper;
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
public class TickedController {

    private TickedServices tickedServices;
    private TickedMapper tickedMapper;

    public TickedController(TickedServices tickedServices, TickedMapper tickedMapper) {
        this.tickedServices = tickedServices;
        this.tickedMapper = tickedMapper;
    }

    @GetMapping()
    public ResponseEntity<PageResponseDTO<TickedResponseDTO>> getAllTicked(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size) {

        Page<TickedResponseDTO> dtoPage = tickedServices.getAllTickeds(page, size).map(tickedMapper::toDto);

        return ResponseEntity.ok(new PageResponseDTO<>(dtoPage));
    }

    @PostMapping("/create")
    public ResponseEntity<GlobalResponseDTO> createTicked(@Valid @RequestBody CreateTickedRequestDTO request) {
        tickedServices.createTicked(request);
        return ResponseEntity.ok(new GlobalResponseDTO("Ticked creado", HttpStatus.CREATED.value()));
    }

}
