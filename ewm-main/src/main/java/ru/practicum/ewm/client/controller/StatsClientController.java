package ru.practicum.ewm.client.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class StatsClientController {
    private final StatsClient statsClient;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody @Valid EndpointHitCreateDto endpointHitCreateDto) {
        log.info("Statistic-client: received request to create new hit '{}'", endpointHitCreateDto);
        statsClient.saveHit(endpointHitCreateDto);
        log.info("Statistic-client: new hit has been created");
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "") List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        log.info("Statistic-client: received request to get stats from {} to {}", start, end);
        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, unique);
        log.info("Statistic-client: stats has been received: {}", stats.toString());
        return stats;
    }
}