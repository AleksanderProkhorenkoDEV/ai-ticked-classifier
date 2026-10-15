package dev.aprokhorenko.ticked_classifier.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import dev.aprokhorenko.ticked_classifier.dto.TicketStatsDTO;
import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;
import dev.aprokhorenko.ticked_classifier.exceptions.InappropriateContentException;
import dev.aprokhorenko.ticked_classifier.exceptions.ModelCallFailedException;
import dev.aprokhorenko.ticked_classifier.exceptions.ModerationUnavailableException;
import dev.aprokhorenko.ticked_classifier.mappers.TicketMapper;
import dev.aprokhorenko.ticked_classifier.services.TicketServices;
import jakarta.persistence.EntityNotFoundException;

// Loads only the web layer: no database and no LLM. Note the package, it moved in Spring
// Boot 4 (org.springframework.boot.webmvc.test.autoconfigure); the old one no longer exists.
@WebMvcTest(controllers = TicketController.class)
// TicketMapper is a @Component so the slice does not pick it up. Importing it means the real
// entity to DTO mapping is exercised instead of a mock that would return nulls.
@Import(TicketMapper.class)
// Webconfig is a WebMvcConfigurer, so the slice DOES load it, and it fails to start without this.
// The rate limit is raised too: every request here comes from the same remote address, and the
// filter is covered on its own by CreateTicketRateLimitFilterTest.
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:5173",
        "app.rate-limit.create-ticket.max-requests=1000" })
class TicketControllerTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2026, 1, 15, 10, 30);

    // @MockBean was removed in Spring Boot 4, @MockitoBean is the replacement.
    // The service is mocked, so these tests never reach the database or the model.
    @MockitoBean
    private TicketServices tickedServices;

    @Autowired
    private MockMvcTester mvc;

    private Ticket ticket() {
        Ticket ticket = new Ticket("Error al subir imagenes", "Sale un error 500",
                TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO);
        ticket.setId(7L);
        // Pinned so the JSON comparison below is deterministic.
        ticket.setDate(FIXED_DATE);
        return ticket;
    }

    /* MockMvcTester has no hasStatus400()/hasStatus404() shorthands, only hasStatus(HttpStatus). */
    /* ---- GET /tickets ---- */

    @Test
    @DisplayName("GET /tickets devuelve la pagina con el contenido mapeado a DTO")
    void getAllReturnsThePage() {
        when(tickedServices.getAllTickeds(eq(0), eq(10), eq(null), eq(null), eq(null)))
                .thenReturn(new PageImpl<>(List.of(ticket()), PageRequest.of(0, 10), 1));

        assertThat(mvc.get().uri("/tickets?page=0&size=10"))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo("""
                        {
                          "content": [
                            {
                              "id": 7,
                              "title": "Error al subir imagenes",
                              "description": "Sale un error 500",
                              "date": "2026-01-15T10:30:00",
                              "category": "BUG",
                              "urgency": "ALTA",
                              "feeling": "NEGATIVO",
                              "status": "ABIERTO"
                            }
                          ],
                          "currentPage": 0,
                          "totalPages": 1,
                          "totalElements": 1
                        }
                        """);
    }

    @Test
    @DisplayName("GET /tickets aplica los filtros de urgencia, categoria y busqueda")
    void getAllPassesTheFilters() {
        when(tickedServices.getAllTickeds(eq(1), eq(5), eq(TicketUrgency.ALTA), eq(TicketCategory.BUG), eq("error")))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(mvc.get().uri("/tickets?page=1&size=5&urgency=ALTA&category=BUG&search=error"))
                .hasStatusOk();

        verify(tickedServices).getAllTickeds(1, 5, TicketUrgency.ALTA, TicketCategory.BUG, "error");
    }

    @Test
    @DisplayName("GET /tickets sin parametros usa la primera pagina de 10")
    void getAllUsesDefaultPaging() {
        when(tickedServices.getAllTickeds(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        assertThat(mvc.get().uri("/tickets")).hasStatusOk();

        verify(tickedServices).getAllTickeds(0, 10, null, null, null);
    }

    @Test
    @DisplayName("GET /tickets con una urgencia invalida devuelve 400")
    void getAllRejectsAnInvalidFilter() {
        assertThat(mvc.get().uri("/tickets?urgency=NO_EXISTE"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    /* ---- POST /tickets/create ---- */

    @Test
    @DisplayName("POST /tickets/create crea el ticket y responde con el mensaje global")
    void createReturnsTheGlobalResponse() {
        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error al subir imagenes","description":"Sale un error 500"}
                        """))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo("""
                        {"message":"Ticked creado","status":201}
                        """);

        verify(tickedServices).createTicked(any());
    }

    @Test
    @DisplayName("POST /tickets/create sin titulo devuelve 400 con el mensaje de validacion")
    void createRejectsAnEmptyTitle() {
        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"","description":"Sale un error 500"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("title: Este campo no puede estar vacío ni ser solo espacios");

        verifyNoInteractions(tickedServices);
    }

    @Test
    @DisplayName("POST /tickets/create sin descripcion devuelve 400")
    void createRejectsAnEmptyDescription() {
        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error","description":""}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("description: Este campo no puede estar vacío ni ser solo espacios");

        verifyNoInteractions(tickedServices);
    }

    // @NotBlank instead of @NotEmpty: a field full of spaces is not a description either.
    @Test
    @DisplayName("POST /tickets/create con solo espacios devuelve 400")
    void createRejectsAWhitespaceOnlyDescription() {
        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error","description":"     "}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(tickedServices);
    }

    // The limit matches the varchar(255) column, so an oversized ticket is refused with a
    // readable message instead of blowing up later as a SQL "data too long".
    @Test
    @DisplayName("POST /tickets/create con una descripcion demasiado larga devuelve 400")
    void createRejectsAnOversizedDescription() {
        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error","description":"%s"}
                        """.formatted("a".repeat(256))))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("description: La descripción no puede superar los 255 caracteres");

        verifyNoInteractions(tickedServices);
    }

    @Test
    @DisplayName("POST /tickets/create con contenido inapropiado devuelve 400 con el motivo")
    void createRejectsInappropriateContent() {
        doThrow(new InappropriateContentException()).when(tickedServices).createTicked(any());

        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error al subir imagenes","description":"Sale un error 500"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .isEqualTo("""
                        {"message":"El contenido del ticket no es apropiado","status":400}
                        """);
    }

    // Failing closed: a moderation outage must not turn into a silent pass.
    @Test
    @DisplayName("POST /tickets/create devuelve 503 si la moderacion no esta disponible")
    void createReturnsServiceUnavailableWhenTheModerationIsDown() {
        doThrow(new ModerationUnavailableException(new RuntimeException("connection reset")))
                .when(tickedServices).createTicked(any());

        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error al subir imagenes","description":"Sale un error 500"}
                        """))
                .hasStatus(HttpStatus.SERVICE_UNAVAILABLE)
                .bodyJson()
                .isEqualTo("""
                        {"message":"No se pudo verificar el contenido del ticket, inténtalo más tarde","status":503}
                        """);
    }

    // Before this handler the failure escaped as the default Boot error body, so the frontend had
    // to understand two different response shapes.
    @Test
    @DisplayName("POST /tickets/create devuelve 502 cuando falla el clasificador")
    void createReturnsBadGatewayWhenTheClassifierFails() {
        doThrow(new ModelCallFailedException("No se pudo obtener respuesta del LLM", new RuntimeException()))
                .when(tickedServices).createTicked(any());

        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error al subir imagenes","description":"Sale un error 500"}
                        """))
                .hasStatus(HttpStatus.BAD_GATEWAY)
                .bodyJson()
                .isEqualTo("""
                        {"message":"No se pudo completar la clasificación, inténtalo de nuevo","status":502}
                        """);
    }

    /* ---- PATCH /tickets/update-status/{id} ---- */

    @Test
    @DisplayName("PATCH /tickets/update-status/{id} actualiza el estado")
    void updateStatusReturnsTheGlobalResponse() {
        assertThat(mvc.patch().uri("/tickets/update-status/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"CERRADO"}
                        """))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo("""
                        {"message":"Estatus actualiazdo","status":200}
                        """);

        verify(tickedServices).updateStatus(any(), eq(7L));
    }

    @Test
    @DisplayName("PATCH /tickets/update-status/{id} sin status devuelve 400")
    void updateStatusRejectsAMissingStatus() {
        assertThat(mvc.patch().uri("/tickets/update-status/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":null}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("status: Este campo tiene que existir");

        verifyNoInteractions(tickedServices);
    }

    @Test
    @DisplayName("PATCH /tickets/update-status/{id} con un status desconocido devuelve 400")
    void updateStatusRejectsAnUnknownStatus() {
        assertThat(mvc.patch().uri("/tickets/update-status/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"INVENTADO"}
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(tickedServices);
    }

    @Test
    @DisplayName("PATCH /tickets/update-status/{id} de un id inexistente devuelve 404")
    void updateStatusReturnsNotFound() {
        doThrow(new EntityNotFoundException()).when(tickedServices).updateStatus(any(), eq(99L));

        assertThat(mvc.patch().uri("/tickets/update-status/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"status":"CERRADO"}
                        """))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .isEqualTo("""
                        {"message":"Entidad no encontrada","status":404}
                        """);
    }

    /* ---- GET /tickets/stats ---- */

    @Test
    @DisplayName("GET /tickets/stats devuelve los cuatro contadores")
    void statsReturnsTheCounters() {
        when(tickedServices.getTicketStats()).thenReturn(new TicketStatsDTO(12L, 7L, 3L, 2L));

        assertThat(mvc.get().uri("/tickets/stats"))
                .hasStatusOk()
                .bodyJson()
                .isEqualTo("""
                        {"total":12,"open":7,"high":3,"resolved":2}
                        """);
    }
}