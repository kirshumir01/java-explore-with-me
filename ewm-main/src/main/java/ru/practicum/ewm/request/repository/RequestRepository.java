package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    @EntityGraph("request-user-event")
    List<Request> findAllByUserId(long userId);

    @EntityGraph("request-user-event")
    List<Request> findAllByEventId(long eventId);

    @EntityGraph("request-user-event")
    Optional<Request> findByIdAndUserId(long requestId, long userId);

    @EntityGraph("request-user-event")
    List<Request> findByIdIn(Set<Long> ids);

    List<Request> findByEventAndUser(Event event, User user);

    @Query("""
            SELECT (COUNT(r.id) >= ?2)
            FROM Request r
            WHERE r.event.id = ?1 AND r.status= 'CONFIRMED'
            """)
    boolean isParticipantLimitReached(long eventId, int limit);
}