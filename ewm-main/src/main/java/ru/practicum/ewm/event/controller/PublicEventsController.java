package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.event.controller.params.PublicEventRequestParams;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventsController {
    private final EventService eventService;
    private final StatsClient statsClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllEvents(
            @ModelAttribute(value = "params") PublicEventRequestParams params,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        log.info("Main-service: received PUBLIC request to GET all events. Predicate: {}", params.toString());
        log.info("Main-service: PUBLIC logging data: client ip: {}, endpoint path: {}", ip, path);
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<EventShortDto> events = eventService.getAllEvents(params, pageRequest);
        log.info("Main-service: events received: {}", events);
        statsClient.saveHit(new EndpointHitCreateDto(
                "ewm-main-service",
                path,
                ip,
                LocalDateTime.now()));
        log.info("Main-service: request to statsClient to save views statistic");
        return events;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable long eventId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        log.info("Main-service: received PUBLIC request to GET event by id = {}", eventId);
        log.info("Main-service: PUBLIC logging data: client ip: {}, endpoint path: {}", ip, path);
        EventFullDto event = eventService.getEventById(eventId);
        log.info("Main-service: event received: {}", event);
        statsClient.saveHit(new EndpointHitCreateDto(
                "ewm-main-service",
                path,
                ip,
                LocalDateTime.now()
        ));
        log.info("Main-service: request to statsClient to save views statistic");
        return event;
    }
}