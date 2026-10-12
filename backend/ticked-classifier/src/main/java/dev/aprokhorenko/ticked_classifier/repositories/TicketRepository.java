package dev.aprokhorenko.ticked_classifier.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.aprokhorenko.ticked_classifier.entities.Ticket;
import dev.aprokhorenko.ticked_classifier.entities.TicketCategory;
import dev.aprokhorenko.ticked_classifier.entities.TicketStatus;
import dev.aprokhorenko.ticked_classifier.entities.TicketUrgency;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    long countByStatus(TicketStatus status);

    long countByUrgency(TicketUrgency urgency);

    long countByArchivedTrue();

    @Query("""
                SELECT t FROM Ticket t
                WHERE (:urgency IS NULL OR t.urgency = :urgency)
                  AND (:category IS NULL OR t.category = :category)
                  AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
                ORDER BY t.date DESC
            """)
    Page<Ticket> findFiltered(@Param("urgency") TicketUrgency urgency,
            @Param("category") TicketCategory category,
            @Param("search") String search,
            Pageable pageable);
}
