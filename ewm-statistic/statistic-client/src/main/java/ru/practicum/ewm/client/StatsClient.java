package ru.practicum.ewm.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class StatsClient {
    private final RestClient restClient;


    @Autowired
    public StatsClient(@Value("${statsclient.url}") String url) {
        restClient = RestClient.builder()
                .baseUrl(url)
                .build();
    }

    public ResponseEntity<Object> saveHit(EndpointHitCreateDto endpointHitCreateDto) {
        return restClient.post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitCreateDto)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    public List<ViewStatsDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique) {
        Map<String, Object> pathParams = Map.of(
                "start", encodeDate(start),
                "end", encodeDate(end),
                "uris", uris,
                "unique", unique
        );

        return restClient.get()
                .uri("/stats", pathParams)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    private String encodeDate(LocalDateTime dateTime) {
        String formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return URLEncoder.encode(formatted, StandardCharsets.UTF_8);
    }
}
