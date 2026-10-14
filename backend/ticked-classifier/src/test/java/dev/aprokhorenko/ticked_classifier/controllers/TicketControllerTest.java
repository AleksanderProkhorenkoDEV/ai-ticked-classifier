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
import dev.aprokhorenko.ticked_classifier.mappers.TicketMapper;
import dev.aprokhorenko.ticked_classifier.services.TicketServices;
import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(controllers = TicketController.class)
@Import(TicketMapper.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class TicketControllerTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2026, 1, 15, 10, 30);

    @MockitoBean
    private TicketServices tickedServices;

    @Autowired
    private MockMvcTester mvc;

    private Ticket ticket() {
        Ticket ticket = new Ticket("Error al subir imagenes", "Sale un error 500",
                TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO);
        ticket.setId(7L);
        ticket.setDate(FIXED_DATE);
        return ticket;
    }

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
                .isEqualTo("title: Este campo no puede estar vacío");

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
                .isEqualTo("description: Este campo no puede estar vacío");

        verifyNoInteractions(tickedServices);
    }

    @Test
    @DisplayName("POST /tickets/create deja escapar el fallo del clasificador")
    void createPropagatesTheClassifierFailure() {
        doThrow(new RuntimeException("No se pudo obtener respuesta del LLM"))
                .when(tickedServices).createTicked(any());

        assertThat(mvc.post().uri("/tickets/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Error al subir imagenes","description":"Sale un error 500"}
                        """))
                .hasFailed()
                .failure()
                .hasRootCauseMessage("No se pudo obtener respuesta del LLM");
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