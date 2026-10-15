package dev.aprokhorenko.ticked_classifier.exceptions;

/**
 * The model did not answer with a usable classification. Kept as its own type so the handler does
 * not have to catch every {@link RuntimeException}: doing that would also swallow the framework
 * failures that Spring already maps to a 400.
 */
public class ModelCallFailedException extends RuntimeException {

    public ModelCallFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}