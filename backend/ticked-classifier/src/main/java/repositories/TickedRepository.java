package repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import entities.Ticked;

public interface TickedRepository extends JpaRepository<Ticked, Long> {
}
