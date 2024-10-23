package ru.practicum.ewm.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.dto.EndpointHitCreateDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.event.controller.params.AdminEventRequestParams;
import ru.practicum.ewm.event.controller.params.PublicEventRequestParams;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.EventDateException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.URIFormatException;
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final CategoryRepository categoryRepository;

    @Override
    public List<EventShortDto> privateGetAllEventsByUserId(long userId, int from, int size) {
        Pageable page = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events = eventRepository.findAll(QEvent.event.initiator.id.eq(userId), page).getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        setEventViewsStatistic(events);

        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto privateCreateEvent(long userId, NewEventDto newEventDto) {
        Event event = EventMapper.toEvent(newEventDto);

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not found", userId)));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Category with id = %d not found", newEventDto.getCategory())));

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new EventDateException("Event date has been passed");
        }

        if (newEventDto.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new EventDateException("Less than two hour until the event starts");
        }

        Location location = LocationMapper.toLocation(newEventDto.getLocation());
        locationRepository.save(location);

        event.setInitiator(initiator);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setViews(0L);
        event.setLocation(location);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto privateGetEventByIdAndUserId(long userId, long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(String.format("User with id = %d is not initiator of the event", userId));
        }

        setEventViewsStatistic(List.of(event));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto privateUpdateEventById(long userId, long eventId, UpdateEventUserRequest updateDto) {
        Event eventToUpdate = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found.", eventId)));

        if (eventToUpdate.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Published event couldn't be updated");
        }

        if (updateDto.getEventDate() != null && updateDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new EventDateException("Event date has been passed");
        }

        if (updateDto.getEventDate() != null && updateDto.getEventDate().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new EventDateException("Less than two hours until the event starts");
        }

        validateUpdateEventRequest(updateDto, eventToUpdate);
        validateUpdateEventUserRequest(updateDto, eventToUpdate);

        return EventMapper.toEventFullDto(eventRepository.save(eventToUpdate));
    }

    @Override
    public List<EventFullDto> adminGetAllEvents(AdminEventRequestParams params, int from, int size) {
        Pageable page = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events;

        if (Objects.nonNull(params.getPredicate())) {
            events = eventRepository.findAll(params.getPredicate(), page).getContent();
        } else {
            events = eventRepository.findAll(page).getContent();
        }

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        setEventViewsStatistic(events);

        return events.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto adminUpdateEventById(long eventId, UpdateEventAdminRequest updateDto) {
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found.", eventId)));

        if (updateDto.getStateActionAdmin() != null
                && updateDto.getStateActionAdmin().equals(UpdateEventAdminRequest.StateActionAdmin.REJECT_EVENT)
                && eventToUpdate.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Event couldn't be rejected in a published state");
        }

        if (!eventToUpdate.getState().equals(EventState.PENDING)) {
            throw new ConflictException("Event can only be published in a pending state");
        }

        if (updateDto.getEventDate() != null && updateDto.getEventDate().isBefore(LocalDateTime.now())) {
            throw new EventDateException("Event date has been passed");
        }

        if (updateDto.getEventDate() != null && updateDto.getEventDate().minusHours(1).isBefore(LocalDateTime.now())) {
            throw new EventDateException("Less than one hour until the event starts");
        }

        validateUpdateEventRequest(updateDto, eventToUpdate);
        validateUpdateEventAdminRequest(updateDto, eventToUpdate);

        return EventMapper.toEventFullDto(eventRepository.save(eventToUpdate));
    }

    @Override
    public List<EventShortDto> publicGetAllEvents(
            PublicEventRequestParams params,
            int from,
            int size,
            String ip,
            String path) {
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events = eventRepository.findAll(params.getPredicate(), pageRequest.withSort(params.getSort())).getContent();

        statsClient.saveHit(new EndpointHitCreateDto(
                "ewm-main-service",
                path,
                ip,
                LocalDateTime.now()));

        setEventViewsStatistic(events);

        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto publicGetEventById(long eventId, String ip, String path) {
        BooleanExpression conditions = QEvent.event.id.eq(eventId).and(QEvent.event.state.eq(EventState.PUBLISHED));

        Event event = eventRepository.findOne(conditions)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        statsClient.saveHit(new EndpointHitCreateDto(
                "ewm-main-service",
                path,
                ip,
                LocalDateTime.now()
        ));

        setEventViewsStatistic(List.of(event));

        return EventMapper.toEventFullDto(event);
    }

    private void setEventViewsStatistic(List<Event> events) {
        String regexp = "/events/";
        List<String> uris = events.stream().map(Event::getId).map(id -> regexp + id).toList();
        LocalDateTime start = events.stream()
                .map(event -> event.getPublishedOn() != null ? event.getPublishedOn() : LocalDateTime.now().minusDays(7))
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(7));
        List<ViewStatsDto> stats = statsClient.getStats(start, LocalDateTime.now(), uris, true);
        Map<Long, Event> eventsMap = events.stream().collect(Collectors.toMap(Event::getId, event ->  event));

        for (ViewStatsDto stat : stats) {
            String uri = stat.getUri();
            if (uri.startsWith(regexp)) {
                try {
                    long eventId = Long.parseLong(uri.substring(regexp.length()));
                    Event event = eventsMap.get(eventId);
                    if (event != null) {
                        event.setViews(stat.getHits());
                    }
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid event ID format in path: " + uri);
                }
            } else {
                throw new URIFormatException("Illegal character in path: " + uri);
            }
        }
    }

    private void validateUpdateEventRequest(UpdateEventDto updateDto, Event event) {
        if (updateDto.getEventDate() != null && updateDto.getEventDate().isAfter(LocalDateTime.now())) {
            event.setEventDate(updateDto.getEventDate());
        }

        if (updateDto.getAnnotation() != null && !updateDto.getAnnotation().isBlank()) {
            event.setAnnotation(updateDto.getAnnotation());
        }

        if (updateDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateDto.getCategory())
                    .orElseThrow(() -> new NotFoundException(String.format("Category with id = %d not found", updateDto.getCategory())));
            event.setCategory(category);
        }

        if (updateDto.getDescription() != null && !updateDto.getDescription().isBlank()) {
            event.setDescription(updateDto.getDescription());
        }

        if (updateDto.getLocation() != null) {
            Location newLocation = LocationMapper.toLocation(updateDto.getLocation());
            locationRepository.save(newLocation);
            event.setLocation(newLocation);
        }

        if (updateDto.getPaid() != null) {
            event.setPaid(updateDto.getPaid());
        }

        if (updateDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateDto.getParticipantLimit());
        }

        if (updateDto.getRequestModeration() != null) {
            event.setRequestModeration(updateDto.getRequestModeration());
        }

        if (updateDto.getTitle() != null && !updateDto.getTitle().isBlank()) {
            event.setTitle(updateDto.getTitle());
        }
    }

    private void validateUpdateEventUserRequest(UpdateEventUserRequest updateDto, Event event) {
        if (Objects.nonNull(updateDto.getStateActionUser())) {
            if (updateDto.getStateActionUser().equals(UpdateEventUserRequest.StateActionUser.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            } else if (updateDto.getStateActionUser().equals(UpdateEventUserRequest.StateActionUser.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
        }
    }

    private void validateUpdateEventAdminRequest(UpdateEventAdminRequest updateDto, Event event) {
        if (Objects.nonNull(updateDto.getStateActionAdmin())) {
            if (updateDto.getStateActionAdmin().equals(UpdateEventAdminRequest.StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                event.setState(EventState.CANCELED);
            }
        }
    }
}