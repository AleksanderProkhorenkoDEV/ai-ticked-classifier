package dev.aprokhorenko.ticked_classifier.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.aprokhorenko.ticked_classifier.dto.GlobalResponseDTO;
import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GlobalResponseDTO> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GlobalResponseDTO("Entidad no encontrada", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalResponseDTO> handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();

        return ResponseEntity.badRequest()
                .body(new GlobalResponseDTO(cause.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }
}
