package dev.aprokhorenko.ticked_classifier.exceptions;

public class ModerationUnavailableException extends RuntimeException {

    public ModerationUnavailableException(Throwable cause) {
        super("No se pudo verificar el contenido del ticket, inténtalo más tarde", cause);
    }
}