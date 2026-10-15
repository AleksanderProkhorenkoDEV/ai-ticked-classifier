package dev.aprokhorenko.ticked_classifier.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import dev.aprokhorenko.ticked_classifier.dto.CreateTicketRequestDTO;
import dev.aprokhorenko.ticked_classifier.dto.TicketClasification;
import dev.aprokhorenko.ticked_classifier.dto.TicketStatsDTO;
import dev.aprokhorenko.ticked_classifier.dto.UpdateStatusTicketDTO;
import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;
import dev.aprokhorenko.ticked_classifier.exceptions.InappropriateContentException;
import dev.aprokhorenko.ticked_classifier.repositories.TicketRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TicketServicesTest {

    private static final String TITLE = "Error al subir imagenes";
    private static final String DESCRIPTION = "Al subir una imagen sale un error 500 en la pantalla de perfil.";

    private static final TicketClasification CLASSIFICATION = new TicketClasification(
            TicketCategory.BUG, TicketFeeling.NEGATIVO, TicketUrgency.ALTA);

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketClasificationServices ticketClasificationServices;

    @Mock
    private TicketModerationServices ticketModerationServices;

    @Mock
    private TicketPersistenceServices ticketPersistenceServices;

    private TicketServices service;

    @BeforeEach
    void setUp() {
        service = new TicketServices(ticketRepository, ticketClasificationServices,
                ticketModerationServices, ticketPersistenceServices);
    }

    private CreateTicketRequestDTO createRequest() {
        CreateTicketRequestDTO request = new CreateTicketRequestDTO();
        request.setTitle(TITLE);
        request.setDescription(DESCRIPTION);
        return request;
    }

    private UpdateStatusTicketDTO statusRequest(TicketStatus status) {
        UpdateStatusTicketDTO request = new UpdateStatusTicketDTO();
        request.setStatus(status);
        return request;
    }

    private Ticket existingTicket() {
        return new Ticket(TITLE, DESCRIPTION, TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO);
    }

    /* ---- createTicked ---- */

    @Test
    @DisplayName("createTicked guarda el ticket con la clasificacion que devuelve el LLM")
    void createTickedSavesTheClassifiedTicket() {
        when(ticketClasificationServices.classifyTicket(TITLE, DESCRIPTION)).thenReturn(CLASSIFICATION);

        service.createTicked(createRequest());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketPersistenceServices).saveNew(captor.capture());

        Ticket saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo(TITLE);
        assertThat(saved.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(saved.getCategory()).isEqualTo(TicketCategory.BUG);
        assertThat(saved.getUrgency()).isEqualTo(TicketUrgency.ALTA);
        assertThat(saved.getFeeling()).isEqualTo(TicketFeeling.NEGATIVO);
    }

    @Test
    @DisplayName("El ticket creado nace abierto y sin archivar")
    void newTicketStartsOpenAndNotArchived() {
        when(ticketClasificationServices.classifyTicket(TITLE, DESCRIPTION)).thenReturn(CLASSIFICATION);

        service.createTicked(createRequest());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketPersistenceServices).saveNew(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(TicketStatus.ABIERTO);
        assertThat(captor.getValue().getArchived()).isFalse();
    }

    @Test
    @DisplayName("createTicked manda el titulo y la descripcion al clasificador")
    void createTickedClassifiesWithTheRequestData() {
        when(ticketClasificationServices.classifyTicket(TITLE, DESCRIPTION)).thenReturn(CLASSIFICATION);

        service.createTicked(createRequest());

        verify(ticketClasificationServices).classifyTicket(TITLE, DESCRIPTION);
    }

    @Test
    @DisplayName("createTicked manda el titulo y la descripcion al moderador")
    void createTickedModeratesWithTheRequestData() {
        when(ticketClasificationServices.classifyTicket(TITLE, DESCRIPTION)).thenReturn(CLASSIFICATION);

        service.createTicked(createRequest());

        verify(ticketModerationServices).moderate(TITLE, DESCRIPTION);
    }

    // La moderacion es la puerta de entrada de un contenido que puede leer cualquiera, asi que
    // cuando rechaza el ticket nada debe llegar al clasificador ni a la base de datos.
    @Test
    @DisplayName("Si la moderacion rechaza el ticket no se clasifica ni se guarda")
    void doesNotClassifyNorSaveWhenTheModerationRefuses() {
        doThrow(new InappropriateContentException()).when(ticketModerationServices).moderate(TITLE, DESCRIPTION);

        assertThatThrownBy(() -> service.createTicked(createRequest()))
                .isInstanceOf(InappropriateContentException.class);

        verify(ticketClasificationServices, never()).classifyTicket(any(), any());
        verify(ticketPersistenceServices, never()).saveNew(any());
    }

    @Test
    @DisplayName("Si el LLM falla no se guarda nada")
    void doesNotSaveWhenTheClassifierFails() {
        RuntimeException llmError = new RuntimeException("No se pudo obtener respuesta del LLM");
        when(ticketClasificationServices.classifyTicket(TITLE, DESCRIPTION)).thenThrow(llmError);

        assertThatThrownBy(() -> service.createTicked(createRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No se pudo obtener respuesta del LLM");

        verify(ticketPersistenceServices, never()).saveNew(any());
    }

    @Test
    @DisplayName("createTicked normaliza el texto antes de moderar, clasificar y guardar")
    void createTickedNormalizesTheText() {
        CreateTicketRequestDTO request = createRequest();
        request.setTitle("  Error\u200B al subir\u00A0imagenes  ");
        request.setDescription("Al subir una imagen\n\nsale un error 500 ");

        when(ticketClasificationServices.classifyTicket(any(), any())).thenReturn(CLASSIFICATION);

        service.createTicked(request);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketPersistenceServices).saveNew(captor.capture());

        // The zero width space, the non breaking space, the line breaks and the padding are gone.
        assertThat(captor.getValue().getTitle()).isEqualTo("Error al subir imagenes");
        assertThat(captor.getValue().getDescription()).isEqualTo("Al subir una imagen sale un error 500");

        verify(ticketModerationServices)
                .moderate("Error al subir imagenes", "Al subir una imagen sale un error 500");
        verify(ticketClasificationServices)
                .classifyTicket("Error al subir imagenes", "Al subir una imagen sale un error 500");
    }

    /* ---- getAllTickeds ---- */

    // The JPQL only filters when the parameter is null, so blank input has to be normalised
    // to null in the service. Without this the frontend sending an empty search box would
    // produce a LIKE '%%' filter at best.
    @Test
    @DisplayName("Un search vacio se normaliza a null antes de llegar al repositorio")
    void blankSearchBecomesNull() {
        when(ticketRepository.findFiltered(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllTickeds(0, 10, TicketUrgency.ALTA, null, "   ");
        service.getAllTickeds(0, 10, TicketUrgency.ALTA, null, "");
        service.getAllTickeds(0, 10, TicketUrgency.ALTA, null, null);

        verify(ticketRepository, org.mockito.Mockito.times(3))
                .findFiltered(eq(TicketUrgency.ALTA), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Un search con texto se pasa recortado al repositorio")
    void searchIsTrimmed() {
        when(ticketRepository.findFiltered(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllTickeds(0, 10, null, TicketCategory.BUG, "  error  ");

        verify(ticketRepository).findFiltered(eq(null), eq(TicketCategory.BUG), eq("error"), any(Pageable.class));
    }

    @Test
    @DisplayName("getAllTickeds delega la paginacion al repositorio")
    void delegatesThePagination() {
        Pageable pageable = PageRequest.of(2, 25);
        when(ticketRepository.findFiltered(any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllTickeds(2, 25, null, null, null);

        verify(ticketRepository).findFiltered(eq(null), eq(null), eq(null), eq(pageable));
    }

    /* ---- getTickedById ---- */

    @Test
    @DisplayName("getTickedById devuelve el ticket cuando existe")
    void returnsTheTicketWhenItExists() {
        Ticket ticket = existingTicket();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThat(service.getTickedById(1L)).isSameAs(ticket);
    }

    @Test
    @DisplayName("getTickedById lanza EntityNotFoundException si no existe")
    void throwsWhenTheTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTickedById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /* ---- updateStatus ---- */

    @Test
    @DisplayName("updateStatus cambia el estado y desarchiva")
    void updateStatusUnarchives() {
        Ticket ticket = existingTicket();
        ticket.changeArchived(true);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        service.updateStatus(statusRequest(TicketStatus.CERRADO), 1L);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CERRADO);
        assertThat(ticket.getArchived()).isFalse();
    }

    @Test
    @DisplayName("updateStatus marca como archivado cuando el estado es ARCHIVADO")
    void updateStatusArchives() {
        Ticket ticket = existingTicket();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        service.updateStatus(statusRequest(TicketStatus.ARCHIVADO), 1L);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ARCHIVADO);
        assertThat(ticket.getArchived()).isTrue();
    }

    @Test
    @DisplayName("updateStatus con un id inexistente lanza EntityNotFoundException")
    void updateStatusThrowsWhenTheTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(statusRequest(TicketStatus.CERRADO), 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /* ---- getTicketStats ---- */

    @Test
    @DisplayName("getTicketStats cuenta cada estado con su consulta")
    void buildsTheStatsFromTheRepository() {
        when(ticketRepository.count()).thenReturn(12L);
        when(ticketRepository.countByStatus(TicketStatus.ABIERTO)).thenReturn(7L);
        when(ticketRepository.countByUrgency(TicketUrgency.ALTA)).thenReturn(3L);
        when(ticketRepository.countByArchivedTrue()).thenReturn(2L);

        TicketStatsDTO stats = service.getTicketStats();

        assertThat(stats.total()).isEqualTo(12L);
        assertThat(stats.open()).isEqualTo(7L);
        assertThat(stats.high()).isEqualTo(3L);
        assertThat(stats.resolved()).isEqualTo(2L);
    }
}