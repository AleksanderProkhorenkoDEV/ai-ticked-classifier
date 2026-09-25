package dev.aprokhorenko.ticked_classifier.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.aprokhorenko.ticked_classifier.entities.Ticked;

public interface TickedRepository extends JpaRepository<Ticked, Long> {
}
