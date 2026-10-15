package dev.aprokhorenko.ticked_classifier.exceptions;

public class InappropriateContentException extends RuntimeException {

    public InappropriateContentException() {
        super("El contenido del ticket no es apropiado");
    }
}