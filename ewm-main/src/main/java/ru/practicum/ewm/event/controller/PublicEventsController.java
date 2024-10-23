package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.event.controller.params.PublicEventRequestParams;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;

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
        List<EventShortDto> events = eventService.publicGetAllEvents(params, from, size, ip, path);
        log.info("Main-service: events received: {}", events);
        return events;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable long eventId, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();
        log.info("Main-service: received PUBLIC request to GET event by id = {}", eventId);
        log.info("Main-service: PUBLIC logging data: client ip: {}, endpoint path: {}", ip, path);
        EventFullDto event = eventService.publicGetEventById(eventId, ip, path);
        log.info("Main-service: event received: {}", event);
        return event;
    }
}