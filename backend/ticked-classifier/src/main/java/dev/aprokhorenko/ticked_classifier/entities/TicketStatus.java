package dev.aprokhorenko.ticked_classifier.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TicketStatus {
    ABIERTO, EN_PROGRESO, CERRADO, ARCHIVADO;


    @JsonCreator
    public static TicketStatus fromString(String value) {
        for (TicketStatus s : TicketStatus.values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Estatus no válido: " + value);
    }
}
