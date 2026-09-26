package dev.aprokhorenko.ticked_classifier.services;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.ai.mistralai.api.MistralAiApi.ChatCompletionRequest.ResponseFormat;
import org.springframework.stereotype.Service;

import dev.aprokhorenko.ticked_classifier.dto.TicketClasification;
import tools.jackson.databind.ObjectMapper;

@Service
public class TicketClasificationServices {

    private final ChatModel chatModel;

    public TicketClasificationServices(ChatModel chatModel) {
        this.chatModel = chatModel;
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

        ChatResponse response = chatModel.call(new Prompt(prompt, options));
        ObjectMapper mapper = new ObjectMapper();
        TicketClasification result = mapper.readValue(response.getResult().getOutput().getText(),
                TicketClasification.class);
        return result;
    }
}
