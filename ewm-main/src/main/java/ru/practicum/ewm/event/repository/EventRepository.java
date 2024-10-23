package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    @EntityGraph(value = "user-category-location")
    Optional<Event> findByIdAndInitiatorId(long id, long userId);

    Boolean existsByCategoryId(long categoryId);
}
