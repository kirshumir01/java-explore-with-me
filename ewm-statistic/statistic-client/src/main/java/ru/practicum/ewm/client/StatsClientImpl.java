package ru.practicum.ewm.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class StatsClientImpl implements StatsClient {
    private final RestClient restClient;

    @Override
    public void saveHit(EndpointHitCreateDto endpointHitCreateDto) {
        try {
            log.debug("Statistic-client: sending POST request to /hit with payload: {}", endpointHitCreateDto);
            ResponseEntity<String> response = restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHitCreateDto)
                    .retrieve()
                    .toEntity(String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.debug("Statistic-client: request to save hit has been proceed with error: {}", response.getBody());
            } else {
                log.info("Statistic-client: request to save hit was successful.");
            }
        } catch (Exception e) {
            log.error("Statistic-client: exception occurred during request to save hit: ", e);
            throw new RuntimeException("Statistic-client: failed to save hit", e);
        }
    }

    @Override
    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique) {

        String uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", encodeDate(start))
                .queryParam("end", encodeDate(end))
                .queryParam("uris", String.join(",", uris))
                .queryParamIfPresent("unique", Optional.ofNullable(unique))
                .build()
                .toUriString();
        log.debug("Statistic-client: sending GET request to {} with uniqueIp={}", uri, unique);
        try {
            ResponseEntity<List<ViewStatsDto>> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<ViewStatsDto>>() {});

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Statistic-client: request to get stats was successful.");
                return response.getBody();
            } else {
                log.debug("Statistic-client: request to get stats failed with status: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Statistic-client: exception occurred during request to get stats: ", e);
            throw new RuntimeException("Failed to get stats", e);
        }
    }

    private String encodeDate(LocalDateTime dateTime) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return URLEncoder.encode(formatted, StandardCharsets.UTF_8).replace("+", " ").replace("%3A", ":");
    }
}