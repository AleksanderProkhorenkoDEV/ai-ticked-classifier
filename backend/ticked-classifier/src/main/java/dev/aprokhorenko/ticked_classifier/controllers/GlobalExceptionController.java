package dev.aprokhorenko.ticked_classifier.controllers;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.aprokhorenko.ticked_classifier.dto.GlobalResponseDTO;
import dev.aprokhorenko.ticked_classifier.exceptions.InappropriateContentException;
import dev.aprokhorenko.ticked_classifier.exceptions.ModelCallFailedException;
import dev.aprokhorenko.ticked_classifier.exceptions.ModerationUnavailableException;
import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GlobalResponseDTO> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GlobalResponseDTO("Entidad no encontrada", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalResponseDTO> handleIllegalArgument() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalResponseDTO("Valor no válido", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(InappropriateContentException.class)
    public ResponseEntity<GlobalResponseDTO> handleInappropriateContent(InappropriateContentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalResponseDTO(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(ModerationUnavailableException.class)
    public ResponseEntity<GlobalResponseDTO> handleModerationUnavailable(ModerationUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new GlobalResponseDTO(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE.value()));
    }


    @ExceptionHandler(ModelCallFailedException.class)
    public ResponseEntity<GlobalResponseDTO> handleModelFailure(ModelCallFailedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new GlobalResponseDTO("No se pudo completar la clasificación, inténtalo de nuevo",
                        HttpStatus.BAD_GATEWAY.value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalResponseDTO(message, HttpStatus.BAD_REQUEST.value()));
    }
}
