package dev.aprokhorenko.ticked_classifier.services;

import java.util.List;

import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.Generation;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.stereotype.Service;

import dev.aprokhorenko.ticked_classifier.exceptions.InappropriateContentException;
import dev.aprokhorenko.ticked_classifier.exceptions.ModerationUnavailableException;

/**
 * Screens the ticket text through the Mistral moderation endpoint before anything is stored.
 * Every ticket is readable by anyone, so content the provider considers abuse is refused instead
 * of published.
 */
@Service
public class TicketModerationServices {

    private final ModerationModel moderationModel;

    public TicketModerationServices(ModerationModel moderationModel) {
        this.moderationModel = moderationModel;
    }

    public void moderate(String title, String description) {
        ModerationResponse response = requestModeration(title, description);

        List<ModerationResult> results = response.getResults().stream()
                .map(Generation::getOutput)
                .flatMap(moderation -> moderation.getResults().stream())
                .toList();

        // Nothing to judge means nothing was actually checked, so it cannot be treated as clean.
        if (results.isEmpty()) {
            throw new ModerationUnavailableException(null);
        }

        if (results.stream().anyMatch(this::isRefused)) {
            throw new InappropriateContentException();
        }
    }

    // Failing closed: if the check cannot be performed the ticket is not stored, otherwise a
    // provider outage would be enough to publish whatever anyone sends.
    private ModerationResponse requestModeration(String title, String description) {
        try {
            return moderationModel.call(new ModerationPrompt(title + System.lineSeparator() + description));
        } catch (Exception e) {
            throw new ModerationUnavailableException(e);
        }
    }

    private boolean isRefused(ModerationResult result) {
        if (result.isFlagged()) {
            return true;
        }

        Categories categories = result.getCategories();
        if (categories == null) {
            return false;
        }

        // Jailbreaking counts too: it is the usual way of smuggling instructions into the prompt.
        // The topical categories (health, financial, law) are left out on purpose, a bug report
        // about the payments module is not abuse.
        return categories.isHate() || categories.isHateThreatening()
                || categories.isHarassment() || categories.isHarassmentThreatening()
                || categories.isViolence() || categories.isViolenceGraphic()
                || categories.isSexual() || categories.isSexualMinors()
                || categories.isSelfHarm() || categories.isSelfHarmIntent() || categories.isSelfHarmInstructions()
                || categories.isJailbreaking();
    }
}