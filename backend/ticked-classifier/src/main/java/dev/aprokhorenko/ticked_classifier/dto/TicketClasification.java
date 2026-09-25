package dev.aprokhorenko.ticked_classifier.dto;

import dev.aprokhorenko.ticked_classifier.entities.TickedCategory;
import dev.aprokhorenko.ticked_classifier.entities.TickedFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TickedUrgency;

public record TicketClasification(TickedCategory category, TickedFeeling feeling, TickedUrgency urgency) {}
