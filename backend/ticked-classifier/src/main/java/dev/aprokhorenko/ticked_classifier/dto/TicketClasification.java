package dev.aprokhorenko.ticked_classifier.dto;

import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;

public record TicketClasification(TicketCategory category, TicketFeeling feeling, TicketUrgency urgency) {}
