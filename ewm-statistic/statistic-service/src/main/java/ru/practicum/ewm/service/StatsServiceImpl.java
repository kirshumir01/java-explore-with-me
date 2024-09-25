package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.mapper.EndpointHitMapper;
import ru.practicum.ewm.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public void saveHit(EndpointHitCreateDto endpointHitCreateDto) {
        statsRepository.save(EndpointHitMapper.toEndpointHit(endpointHitCreateDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (start.isAfter(end)) {
            throw new BadRequestException(String.format("End time %s is earlier than start time $s.", end, start));
        }

        if (unique && !uris.isEmpty()) {
            return statsRepository.findAllHitsWithUniqueIpWithUris(uris, start, end);
        }

        if (unique) {
            return statsRepository.findAllHitsWithUniqueIpWithoutUris(start, end);
        }

        if (!uris.isEmpty()) {
            return statsRepository.findAllHitsWithUris(uris, start, end);
        }

        return statsRepository.findAllHitsWithoutUris(start, end);
    }
}
