package dev.aprokhorenko.ticked_classifier.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import dev.aprokhorenko.ticked_classifier.repositories.TicketRepository;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class TicketServicesTest {

    private static final String TITLE = "Error al subir imagenes";
    private static final String DESCRIPTION = "Al subir una imagen sale un error 500 en la pantalla de perfil.";

    private static final TicketClasification CLASSIFICATION = new TicketClasification(
            TicketCategory.BUG, TicketFeeling.NEGATIVO, TicketUrgency.ALTA);

    @Mock
    private TicketRepository tickedRepository;

    @Mock
    private TicketClasificationServices ticketClasificationServices;

    private TicketServices service;

    @BeforeEach
    void setUp() {
        service = new TicketServices(tickedRepository, ticketClasificationServices);
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
        verify(tickedRepository).save(captor.capture());

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
        verify(tickedRepository).save(captor.capture());

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
    @DisplayName("Si el LLM falla no se guarda nada")
    void doesNotSaveWhenTheClassifierFails() {
        RuntimeException llmError = new RuntimeException("No se pudo obtener respuesta del LLM");
        when(ticketClasificationServices.classifyTicket(TITLE, DESCRIPTION)).thenThrow(llmError);

        assertThatThrownBy(() -> service.createTicked(createRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No se pudo obtener respuesta del LLM");

        verify(tickedRepository, never()).save(any());
    }

    /* ---- getAllTickeds ---- */

    @Test
    @DisplayName("Un search vacio se normaliza a null antes de llegar al repositorio")
    void blankSearchBecomesNull() {
        when(tickedRepository.findFiltered(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllTickeds(0, 10, TicketUrgency.ALTA, null, "   ");
        service.getAllTickeds(0, 10, TicketUrgency.ALTA, null, "");
        service.getAllTickeds(0, 10, TicketUrgency.ALTA, null, null);

        verify(tickedRepository, org.mockito.Mockito.times(3))
                .findFiltered(eq(TicketUrgency.ALTA), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("Un search con texto se pasa recortado al repositorio")
    void searchIsTrimmed() {
        when(tickedRepository.findFiltered(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllTickeds(0, 10, null, TicketCategory.BUG, "  error  ");

        verify(tickedRepository).findFiltered(eq(null), eq(TicketCategory.BUG), eq("error"), any(Pageable.class));
    }

    @Test
    @DisplayName("getAllTickeds delega la paginacion al repositorio")
    void delegatesThePagination() {
        Pageable pageable = PageRequest.of(2, 25);
        when(tickedRepository.findFiltered(any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        service.getAllTickeds(2, 25, null, null, null);

        verify(tickedRepository).findFiltered(eq(null), eq(null), eq(null), eq(pageable));
    }

    /* ---- getTickedById ---- */

    @Test
    @DisplayName("getTickedById devuelve el ticket cuando existe")
    void returnsTheTicketWhenItExists() {
        Ticket ticket = existingTicket();
        when(tickedRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThat(service.getTickedById(1L)).isSameAs(ticket);
    }

    @Test
    @DisplayName("getTickedById lanza EntityNotFoundException si no existe")
    void throwsWhenTheTicketDoesNotExist() {
        when(tickedRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTickedById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /* ---- updateStatus ---- */

    @Test
    @DisplayName("updateStatus cambia el estado y desarchiva")
    void updateStatusUnarchives() {
        Ticket ticket = existingTicket();
        ticket.changeArchived(true);
        when(tickedRepository.findById(1L)).thenReturn(Optional.of(ticket));

        service.updateStatus(statusRequest(TicketStatus.CERRADO), 1L);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CERRADO);
        assertThat(ticket.getArchived()).isFalse();
    }

    @Test
    @DisplayName("updateStatus marca como archivado cuando el estado es ARCHIVADO")
    void updateStatusArchives() {
        Ticket ticket = existingTicket();
        when(tickedRepository.findById(1L)).thenReturn(Optional.of(ticket));

        service.updateStatus(statusRequest(TicketStatus.ARCHIVADO), 1L);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ARCHIVADO);
        assertThat(ticket.getArchived()).isTrue();
    }

    @Test
    @DisplayName("updateStatus con un id inexistente lanza EntityNotFoundException")
    void updateStatusThrowsWhenTheTicketDoesNotExist() {
        when(tickedRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(statusRequest(TicketStatus.CERRADO), 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /* ---- getTicketStats ---- */

    @Test
    @DisplayName("getTicketStats cuenta cada estado con su consulta")
    void buildsTheStatsFromTheRepository() {
        when(tickedRepository.count()).thenReturn(12L);
        when(tickedRepository.countByStatus(TicketStatus.ABIERTO)).thenReturn(7L);
        when(tickedRepository.countByUrgency(TicketUrgency.ALTA)).thenReturn(3L);
        when(tickedRepository.countByArchivedTrue()).thenReturn(2L);

        TicketStatsDTO stats = service.getTicketStats();

        assertThat(stats.total()).isEqualTo(12L);
        assertThat(stats.open()).isEqualTo(7L);
        assertThat(stats.high()).isEqualTo(3L);
        assertThat(stats.resolved()).isEqualTo(2L);
    }
}