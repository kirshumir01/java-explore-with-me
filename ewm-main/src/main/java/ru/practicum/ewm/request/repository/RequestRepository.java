package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
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
            SELECT (COUNT(r.id) >= :limit)
            FROM Request r
            WHERE r.event.id = :eventId AND r.status= 'CONFIRMED'
            """)
    boolean isParticipantLimitReached(@Param("eventId") long eventId,
                                      @Param("limit") int limit);

    @Query("""
            SELECT r FROM Request r
            WHERE r.event.id IN :eventIds AND r.status = :status
            """)
    @EntityGraph("request-user-event")
    List<Request> findAllByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds,
                                             @Param("status") RequestStatus status);

    @Query("""
            SELECT COUNT(r) FROM Request r
            WHERE r.event.id = :eventId AND r.status = :status
            """)
    Integer countByEventIdsAndStatus(@Param("eventId") long eventId,
                                     @Param("status") RequestStatus status);

    @Query("""
            SELECT r.event.id FROM Request r
            JOIN Event e ON r.event.id = e.id
            WHERE r.status = 'CONFIRMED'
            GROUP BY r.event.id, e.participantLimit
            HAVING COUNT(r) < e.participantLimit
            """)
    List<Long> findEventIdsWithAvailableSpots();
}