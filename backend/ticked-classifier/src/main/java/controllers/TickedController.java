package controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.PageResponseDTO;
import dto.TickedResponseDTO;
import mappers.TickedMapper;
import services.TickedServices;

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
