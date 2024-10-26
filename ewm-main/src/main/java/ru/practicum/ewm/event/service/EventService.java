package ru.practicum.ewm.event.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.event.controller.params.AdminEventRequestParams;
import ru.practicum.ewm.event.controller.params.PublicEventRequestParams;
import ru.practicum.ewm.event.dto.*;

import java.util.List;

public interface EventService {

    List<EventShortDto> getAllEventsByUserId(long userId, PageRequest pageRequest);

    EventFullDto createEvent(long userId, NewEventDto newEventDto);

    EventFullDto getEventByIdAndUserId(long userId, long eventId);

    EventFullDto updateEventById(long userId, long eventId, UpdateEventUserRequest updateDto);

    List<EventFullDto> getAllEvents(AdminEventRequestParams params, PageRequest pageRequest);

    EventFullDto updateEventById(long eventId, UpdateEventAdminRequest updateDto);

    List<EventShortDto> getAllEvents(PublicEventRequestParams params, PageRequest pageRequest);

    EventFullDto getEventById(long eventId);
}