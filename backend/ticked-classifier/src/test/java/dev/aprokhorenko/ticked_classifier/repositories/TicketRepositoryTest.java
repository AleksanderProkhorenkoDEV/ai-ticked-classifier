package dev.aprokhorenko.ticked_classifier.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import dev.aprokhorenko.ticked_classifier.TestcontainersConfiguration;
import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketFeeling;
import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;
import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class TicketRepositoryTest {

    @Autowired
    private TicketRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Ticket ticket(String title, TicketCategory category, TicketUrgency urgency, TicketFeeling feeling) {
        return new Ticket(title, "Descripcion de " + title, category, urgency, feeling);
    }

    private void saveAndFlush(Ticket... tickets) {
        repository.saveAllAndFlush(List.of(tickets));
        entityManager.clear();
    }

    @Test
    @DisplayName("findFiltered sin filtros devuelve todos los tickets")
    void findFilteredWithoutFiltersReturnsEverything() {
        saveAndFlush(
                ticket("Error al subir", TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO),
                ticket("Consulta de precios", TicketCategory.CONSULTA, TicketUrgency.BAJA, TicketFeeling.NEUTRO));

        Page<Ticket> page = repository.findFiltered(null, null, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findFiltered filtra por urgencia")
    void findFilteredByUrgency() {
        saveAndFlush(
                ticket("Error al subir", TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO),
                ticket("Consulta de precios", TicketCategory.CONSULTA, TicketUrgency.BAJA, TicketFeeling.NEUTRO));

        Page<Ticket> page = repository.findFiltered(TicketUrgency.ALTA, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Ticket::getTitle).containsExactly("Error al subir");
    }

    @Test
    @DisplayName("findFiltered filtra por categoria")
    void findFilteredByCategory() {
        saveAndFlush(
                ticket("Error al subir", TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO),
                ticket("Caida general", TicketCategory.INCIDENCIA, TicketUrgency.ALTA, TicketFeeling.NEGATIVO));

        Page<Ticket> page = repository.findFiltered(null, TicketCategory.INCIDENCIA, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Ticket::getTitle).containsExactly("Caida general");
    }

    @Test
    @DisplayName("findFiltered busca en el titulo sin distinguir mayusculas")
    void findFilteredSearchesTheTitleIgnoringCase() {
        saveAndFlush(
                ticket("Error de AUTENTICACION", TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO),
                ticket("Consulta de precios", TicketCategory.CONSULTA, TicketUrgency.BAJA, TicketFeeling.NEUTRO));

        Page<Ticket> page = repository.findFiltered(null, null, "autenticacion", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Ticket::getTitle).containsExactly("Error de AUTENTICACION");
    }

    @Test
    @DisplayName("findFiltered combina los tres filtros")
    void findFilteredCombinesTheFilters() {
        saveAndFlush(
                ticket("Error de pago", TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO),
                ticket("Error de carga", TicketCategory.BUG, TicketUrgency.BAJA, TicketFeeling.NEUTRO),
                ticket("Consulta de pago", TicketCategory.CONSULTA, TicketUrgency.ALTA, TicketFeeling.NEUTRO));

        Page<Ticket> page = repository
                .findFiltered(TicketUrgency.ALTA, TicketCategory.BUG, "pago", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Ticket::getTitle).containsExactly("Error de pago");
    }

    @Test
    @DisplayName("findFiltered ordena del mas nuevo al mas viejo")
    void findFilteredSortsByDateDescending() {
        Ticket old = ticket("Antiguo", TicketCategory.BUG, TicketUrgency.BAJA, TicketFeeling.NEUTRO);
        old.setDate(LocalDateTime.of(2026, 1, 1, 9, 0));
        Ticket recent = ticket("Reciente", TicketCategory.BUG, TicketUrgency.BAJA, TicketFeeling.NEUTRO);
        recent.setDate(LocalDateTime.of(2026, 1, 2, 9, 0));

        saveAndFlush(old, recent);

        Page<Ticket> page = repository.findFiltered(null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Ticket::getTitle)
                .containsExactly("Reciente", "Antiguo");
    }

    @Test
    @DisplayName("findFiltered pagina los resultados")
    void findFilteredPaginates() {
        saveAndFlush(
                ticket("Uno", TicketCategory.BUG, TicketUrgency.BAJA, TicketFeeling.NEUTRO),
                ticket("Dos", TicketCategory.BUG, TicketUrgency.BAJA, TicketFeeling.NEUTRO),
                ticket("Tres", TicketCategory.BUG, TicketUrgency.BAJA, TicketFeeling.NEUTRO));

        Page<Ticket> page = repository.findFiltered(null, null, null, PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Los contadores de las estadísticas suman lo que toca")
    void countsTheStats() {
        Ticket abierto = ticket("Abierto", TicketCategory.BUG, TicketUrgency.ALTA, TicketFeeling.NEGATIVO);
        Ticket archivado = ticket("Archivado", TicketCategory.BUG, TicketUrgency.BAJA, TicketFeeling.NEUTRO);
        archivado.changeStatus(TicketStatus.ARCHIVADO);
        archivado.changeArchived(true);

        saveAndFlush(abierto, archivado);

        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.countByStatus(TicketStatus.ABIERTO)).isEqualTo(1);
        assertThat(repository.countByUrgency(TicketUrgency.ALTA)).isEqualTo(1);
        assertThat(repository.countByArchivedTrue()).isEqualTo(1);
    }
}