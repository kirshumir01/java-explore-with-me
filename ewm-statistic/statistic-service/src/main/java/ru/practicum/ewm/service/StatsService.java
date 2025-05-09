package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(EndpointHitCreateDto endpointHitCreateDto);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
