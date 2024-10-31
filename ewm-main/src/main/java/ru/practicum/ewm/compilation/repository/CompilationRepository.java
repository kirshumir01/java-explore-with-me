package ru.practicum.ewm.compilation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long>, QuerydslPredicateExecutor<Compilation> {
    @Query("""
            SELECT c FROM Compilation c
            LEFT JOIN FETCH c.events
            WHERE c.id = :compId
            """)
    Optional<Compilation> findByIdWithEvents(@Param("compId") Long compId);

    @Query(value = "SELECT compilation_id, event_id FROM compilation_events", nativeQuery = true)
    List<Object[]> findAllCompilationEventPairs();
}