package ru.practicum.ewm.event.service;

import ru.practicum.ewm.event.controller.params.AdminEventRequestParams;
import ru.practicum.ewm.event.controller.params.PublicEventRequestParams;
import ru.practicum.ewm.event.dto.*;

import java.util.List;

public interface EventService {

    List<EventShortDto> privateGetAllEventsByUserId(long userId, int from, int size);

    EventFullDto privateCreateEvent(long userId, NewEventDto newEventDto);

    EventFullDto privateGetEventByIdAndUserId(long userId, long eventId);

    EventFullDto privateUpdateEventById(long userId, long eventId, UpdateEventUserRequest updateDto);

    List<EventFullDto> adminGetAllEvents(AdminEventRequestParams params, int from, int size);

    EventFullDto adminUpdateEventById(long eventId, UpdateEventAdminRequest updateDto);

    List<EventShortDto> publicGetAllEvents(PublicEventRequestParams params, int from, int size, String ip, String path);

    EventFullDto publicGetEventById(long eventId, String ip, String path);
}