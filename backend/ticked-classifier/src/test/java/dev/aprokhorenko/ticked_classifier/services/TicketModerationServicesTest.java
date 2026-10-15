package dev.aprokhorenko.ticked_classifier.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.Generation;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;

import dev.aprokhorenko.ticked_classifier.exceptions.InappropriateContentException;
import dev.aprokhorenko.ticked_classifier.exceptions.ModerationUnavailableException;

@ExtendWith(MockitoExtension.class)
class TicketModerationServicesTest {

    private static final String TITLE = "Error al subir imagenes";
    private static final String DESCRIPTION = "Al subir una imagen sale un error 500 en la pantalla de perfil.";

    @Mock
    private ModerationModel moderationModel;

    private TicketModerationServices service;

    @BeforeEach
    void setUp() {
        service = new TicketModerationServices(moderationModel);
    }

    // The provider answers with generations wrapping a moderation, each holding the results.
    private void mockModeration(ModerationResult... results) {
        Moderation moderation = Moderation.builder()
                .id("moderation-1")
                .model("mistral-moderation-latest")
                .results(List.of(results))
                .build();
        when(moderationModel.call(any(ModerationPrompt.class)))
                .thenReturn(new ModerationResponse(new Generation(moderation)));
    }

    private static ModerationResult result(boolean flagged, Categories categories) {
        return ModerationResult.builder().flagged(flagged).categories(categories).build();
    }

    @Test
    @DisplayName("Un ticket limpio pasa la moderacion")
    void acceptsCleanContent() {
        mockModeration(result(false, Categories.builder().build()));

        assertThatCode(() -> service.moderate(TITLE, DESCRIPTION)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("El titulo y la descripcion se envian juntos al moderador")
    void sendsTheTitleAndTheDescription() {
        mockModeration(result(false, Categories.builder().build()));

        service.moderate(TITLE, DESCRIPTION);

        ArgumentCaptor<ModerationPrompt> captor = ArgumentCaptor.forClass(ModerationPrompt.class);
        org.mockito.Mockito.verify(moderationModel).call(captor.capture());

        assertThat(captor.getValue().getInstructions().getText())
                .contains(TITLE)
                .contains(DESCRIPTION);
    }

    @Test
    @DisplayName("Un contenido marcado como flagged se rechaza")
    void rejectsFlaggedContent() {
        mockModeration(result(true, Categories.builder().build()));

        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(InappropriateContentException.class)
                .hasMessage("El contenido del ticket no es apropiado");
    }

    // flagged can stay false while a single category is on, so the categories are checked too.
    @Test
    @DisplayName("Una categoria objectionable basta para rechazar aunque flagged venga a false")
    void rejectsAnOffensiveCategory() {
        mockModeration(result(false, Categories.builder().hate(true).build()));

        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(InappropriateContentException.class);
    }

    @Test
    @DisplayName("Rechaza tambien el resto de categorias de abuso")
    void rejectsOtherAbuseCategories() {
        mockModeration(result(false, Categories.builder().harassment(true).build()));
        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(InappropriateContentException.class);

        mockModeration(result(false, Categories.builder().violence(true).build()));
        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(InappropriateContentException.class);

        mockModeration(result(false, Categories.builder().sexual(true).build()));
        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(InappropriateContentException.class);

        mockModeration(result(false, Categories.builder().jailbreaking(true).build()));
        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(InappropriateContentException.class);
    }

    // A bug report about payments is not abuse, so the topical categories must not reject it.
    @Test
    @DisplayName("Las categorias tematicas no rechazan un ticket legitimo")
    void acceptsTopicalCategories() {
        mockModeration(result(false, Categories.builder().financial(true).health(true).law(true).build()));

        assertThatCode(() -> service.moderate(TITLE, DESCRIPTION)).doesNotThrowAnyException();
    }

    // Failing closed: if the check cannot be performed, the ticket must not be stored.
    @Test
    @DisplayName("Si la moderacion falla se lanza ModerationUnavailableException")
    void wrapsTheProviderFailure() {
        RuntimeException providerError = new RuntimeException("connection reset");
        when(moderationModel.call(any(ModerationPrompt.class))).thenThrow(providerError);

        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(ModerationUnavailableException.class)
                .hasMessage("No se pudo verificar el contenido del ticket, inténtalo más tarde")
                .hasCause(providerError);
    }

    // No results means nothing was judged, which is not the same as a clean verdict.
    @Test
    @DisplayName("Una respuesta sin resultados se trata como moderacion no disponible")
    void treatsAnEmptyVerdictAsUnavailable() {
        mockModeration();

        assertThatThrownBy(() -> service.moderate(TITLE, DESCRIPTION))
                .isInstanceOf(ModerationUnavailableException.class);
    }
}