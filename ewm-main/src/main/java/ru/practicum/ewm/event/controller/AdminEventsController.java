package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.controller.params.AdminEventRequestParams;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventsController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getAllEvents(
            @ModelAttribute(value = "params") AdminEventRequestParams params,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Main-service: received ADMIN request to GET all events. Predicate: {}", params.toString());
        List<EventFullDto> events = eventService.adminGetAllEvents(params, from, size);
        log.info("Main-service: events received: {}", events);
        return events;
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateDto) {
        log.info("Main-service: received ADMIN request to UPDATE event by id = {}, updateDTO = {}", eventId, updateDto);
        EventFullDto event = eventService.adminUpdateEventById(eventId, updateDto);
        log.info("Main-service: event received: {}", event);
        return event;
    }
}