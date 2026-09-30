package dev.aprokhorenko.ticked_classifier.services;

import java.util.logging.Logger;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionRequest.ResponseFormat;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;

import dev.aprokhorenko.ticked_classifier.dto.TicketClasification;
import tools.jackson.databind.ObjectMapper;

@Service
public class TicketClasificationServices {

    private final ChatModel chatModel;
    private final Logger log;
    private final ObjectMapper mapper;

    public TicketClasificationServices(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.log = Logger.getLogger("Ticket Clasifier Services");
        this.mapper = new ObjectMapper();
    }

    public TicketClasification classifyTicket(String title, String description) {
        String prompt = """
                Clasifica el siguiente ticked.
                Titulo: %s
                Descripción: %s
                """.formatted(title, description);

        var options = MistralAiChatOptions.builder()
                .model(MistralAiApi.ChatModel.MINISTRAL_3B.getValue())
                .responseFormat(ResponseFormat.jsonSchema(TicketClasification.class))
                .build();

        String rawOutput;
        try {
            ChatResponse response = chatModel.call(new Prompt(prompt, options));
            rawOutput = response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.severe("Error en la llamada al LLM");
            throw new RuntimeException("No se pudo obtener respuesta del LLM", e);
        }

        try {
            return this.mapper.readValue(rawOutput, TicketClasification.class);
        } catch (JsonParseException e) {
            log.severe("Error al convertir el JSON");
            throw new RuntimeException("El modelo tiene un formato inesperado", e);
        }
    }
}
