package dev.aprokhorenko.ticked_classifier.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.aprokhorenko.ticked_classifier.dto.PageResponseDTO;
import dev.aprokhorenko.ticked_classifier.dto.TickedResponseDTO;
import dev.aprokhorenko.ticked_classifier.mappers.TickedMapper;
import dev.aprokhorenko.ticked_classifier.services.TickedServices;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            @RequestParam String param,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "25") Integer size) {

        Page<TickedResponseDTO> dtoPage = tickedServices.getAllTickeds(page, size).map(tickedMapper::toDto);

        return ResponseEntity.ok(new PageResponseDTO<>(dtoPage));
    }

}
