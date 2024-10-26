package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("users/{userId}/events")
public class PrivateEventsController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getAllEventsByUserId(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Main-service: received PRIVATE request to GET all events by user id: {}", userId);
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        List<EventShortDto> events = eventService.getAllEventsByUserId(userId, pageRequest);
        log.info("Main-service: events received: {}", events);
        return events;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(
            @PathVariable long userId,
            @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Main-service: received PRIVATE request to CREATE event by user: {}", newEventDto);
        EventFullDto createdEvent = eventService.createEvent(userId, newEventDto);
        log.info("Main-service: event created: {}", createdEvent);
        return createdEvent;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByIdAndUserId(
            @PathVariable long userId,
            @PathVariable long eventId) {
        log.info("Main-service: received PRIVATE request to GET event by event id = {} and user id = {}", eventId, userId);
        EventFullDto receivedEvent = eventService.getEventByIdAndUserId(userId, eventId);
        log.info("Main-service: event received: {}", receivedEvent);
        return receivedEvent;
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(
            @PathVariable long userId,
            @PathVariable long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateDto
    ) {
        log.info("Main-service: received PRIVATE request to UPDATE event: {}", updateDto);
        EventFullDto updatedEvent = eventService.updateEventById(userId, eventId, updateDto);
        log.info("Main-service: event updated: {}", updatedEvent);
        return updatedEvent;
    }
}