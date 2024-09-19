package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
            SELECT NEW ru.practicum.ewm.dto.ViewStatsDto(eh.app, eh.uri, COUNT(eh.uri))
            FROM EndpointHit AS eh
            WHERE eh.uri IN (:uris) AND eh.timestamp BETWEEN :start AND :end
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(eh.uri) DESC
            """)
    List<ViewStatsDto> findAllHitsWithUris(@Param("uris") List<String> uris,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("""
            SELECT NEW ru.practicum.ewm.dto.ViewStatsDto(eh.app, eh.uri, COUNT(eh.uri))
            FROM EndpointHit AS eh
            WHERE eh.timestamp BETWEEN :start AND :end
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(eh.uri) DESC
            """)
    List<ViewStatsDto> findAllHitsWithoutUris(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("""
            SELECT NEW ru.practicum.ewm.dto.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip))
            FROM EndpointHit AS eh
            WHERE eh.uri IN (:uris) AND eh.timestamp BETWEEN :start AND :end
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(DISTINCT eh.ip) DESC
            """)
    List<ViewStatsDto> findAllHitsWithUniqueIpWithUris(@Param("uris") List<String> uris,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    @Query("""
            SELECT NEW ru.practicum.ewm.dto.ViewStatsDto(eh.app, eh.uri, COUNT(DISTINCT eh.ip))
            FROM EndpointHit AS eh
            WHERE eh.timestamp BETWEEN :start AND :end
            GROUP BY eh.app, eh.uri
            ORDER BY COUNT(DISTINCT eh.ip) DESC
            """)
    List<ViewStatsDto> findAllHitsWithUniqueIpWithoutUris(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);
}