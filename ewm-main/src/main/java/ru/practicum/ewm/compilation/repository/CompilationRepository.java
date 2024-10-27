package ru.practicum.ewm.compilation.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.compilation.model.Compilation;

import java.util.Optional;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long>, QuerydslPredicateExecutor<Compilation> {
    @Query("""
            SELECT c FROM Compilation c
            LEFT JOIN FETCH c.events
            """)
    Page<Compilation> findAll(Predicate predicate, Pageable page);

    @Query("""
            SELECT c FROM Compilation c
            LEFT JOIN FETCH c.events
            """)
    Page<Compilation> findAll(Pageable page);

    @Query("""
            SELECT c FROM Compilation c
            LEFT JOIN FETCH c.events
            WHERE c.id = :compId
            """)
    Optional<Compilation> findByIdWithEvents(@Param("compId") Long compId);
}