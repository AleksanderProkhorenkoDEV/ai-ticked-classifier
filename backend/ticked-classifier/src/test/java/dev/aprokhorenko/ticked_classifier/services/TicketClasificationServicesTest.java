package dev.aprokhorenko.ticked_classifier.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import tools.jackson.core.JacksonException;

import dev.aprokhorenko.ticked_classifier.dto.TicketClasification;
import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;

@ExtendWith(MockitoExtension.class)
class TicketClasificationServicesTest {

    private static final String TITLE = "Error al subir imagenes";
    private static final String DESCRIPTION = "Al subir una imagen sale un error 500 en la pantalla de perfil.";

    @Mock
    private ChatModel chatModel;

    private TicketClasificationServices service;

    @BeforeEach
    void setUp() {
        service = new TicketClasificationServices(chatModel);
    }

    private void mockLlmAnswer(String rawOutput) {
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage(rawOutput))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
    }

    @Test
    @DisplayName("Devuelve la clasificacion que responde el LLM")
    void returnsTheClassificationFromTheModel() {
        mockLlmAnswer("""
                {"category":"BUG","feeling":"NEGATIVO","urgency":"ALTA"}
                """);

        TicketClasification result = service.classifyTicket(TITLE, DESCRIPTION);

        assertThat(result.category()).isEqualTo(TicketCategory.BUG);
        assertThat(result.feeling()).isEqualTo(TicketFeeling.NEGATIVO);
        assertThat(result.urgency()).isEqualTo(TicketUrgency.ALTA);
    }

    @Test
    @DisplayName("Manda el titulo y la descripcion en el prompt")
    void sendsTheTitleAndDescriptionInThePrompt() {
        mockLlmAnswer("""
                {"category":"CONSULTA","feeling":"NEUTRO","urgency":"BAJA"}
                """);

        service.classifyTicket(TITLE, DESCRIPTION);

        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(captor.capture());

        assertThat(captor.getValue().getContents())
                .contains(TITLE)
                .contains(DESCRIPTION);
    }

    @Test
    @DisplayName("Envuelve el fallo del LLM en un RuntimeException con mensaje propio")
    void wrapsTheLlmFailure() {
        RuntimeException llmError = new RuntimeException("connection reset");
        when(chatModel.call(any(Prompt.class))).thenThrow(llmError);

        assertThatThrownBy(() -> service.classifyTicket(TITLE, DESCRIPTION))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No se pudo obtener respuesta del LLM")
                .hasCause(llmError);
    }

    @Test
    @DisplayName("Envuelve el JSON malformado en un RuntimeException con mensaje propio")
    void wrapsTheMalformedJson() {
        mockLlmAnswer("esto no es un json");

        assertThatThrownBy(() -> service.classifyTicket(TITLE, DESCRIPTION))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El modelo tiene un formato inesperado")
                .cause()
                .isInstanceOf(JacksonException.class);
    }

    @Test
    @DisplayName("Una categoria desconocida es tambien un fallo de formato, no un 400")
    void unknownEnumValueIsReportedAsAFormatError() {
        mockLlmAnswer("""
                {"category":"NO_EXISTE","feeling":"NEUTRO","urgency":"BAJA"}
                """);

        assertThatThrownBy(() -> service.classifyTicket(TITLE, DESCRIPTION))
                .hasMessage("El modelo tiene un formato inesperado")
                .isNotInstanceOf(IllegalArgumentException.class);
    }
}