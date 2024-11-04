package ru.practicum.ewm.event.repository;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.sliceablerepository.SliceableRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>,
        SliceableRepository<Event>,
        QuerydslPredicateExecutor<Event> {
    @EntityGraph(value = "user-category-location")
    Optional<Event> findByIdAndInitiatorId(long id, long userId);

    Boolean existsByCategoryId(long categoryId);

    @EntityGraph(value = "user-category-location")
    List<Event> findAllByIdIn(Set<Long> ids);

    @EntityGraph(value = "user-category-location")
    Slice<Event> findAllSliced(EntityPath path, Predicate predicate, Pageable page);
}
