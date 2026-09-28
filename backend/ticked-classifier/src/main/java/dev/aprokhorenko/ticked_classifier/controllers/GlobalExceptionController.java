package dev.aprokhorenko.ticked_classifier.controllers;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new GlobalResponseDTO(message, HttpStatus.BAD_REQUEST.value()));
    }
}
