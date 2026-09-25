package dev.aprokhorenko.ticked_classifier;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import dev.aprokhorenko.ticked_classifier.services.TickedClasificationServices;

@Component
public class TestRunner implements CommandLineRunner {
    private final TickedClasificationServices classificationService;

    public TestRunner(TickedClasificationServices classificationService) {
        this.classificationService = classificationService;
    }

    @Override
    public void run(String... args) {
        ChatResponse resultado = classificationService.classifyTicket(
                "La app se cierra al subir una foto",
                "Cada vez que subo una foto de perfil, la app se cierra sola. Ya me ha pasado varias veces.");
        System.out.println("=== RESULTADO CLASIFICACIÓN ===");
        System.out.println(resultado);
    }
}
