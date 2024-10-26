package ru.practicum.ewm.event.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.client.StatsClient;
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
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.location.model.Location;
import ru.practicum.ewm.location.repository.LocationRepository;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;

    @Override
    public List<EventShortDto> getAllEventsByUserId(long userId, PageRequest pageRequest) {
        Predicate predicate = QEvent.event.initiator.id.eq(userId);
        List<Event> events = eventRepository.findAll(predicate, pageRequest).getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Request> confirmedRequests = getConfirmedRequests(events);
        List<ViewStatsDto> stats = getViewsStats(events);


        return EventMapper.toEventShortDtoList(events, confirmedRequests, stats);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(long userId, NewEventDto newEventDto) {
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
        event.setLocation(location);
        List<Request> confirmedRequests = new ArrayList<>();
        List<ViewStatsDto> stats = new ArrayList<>();

        Event savedEvent = eventRepository.save(event);

        return EventMapper.toEventFullDto(savedEvent, confirmedRequests, stats);
    }

    @Override
    public EventFullDto getEventByIdAndUserId(long userId, long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(String.format("User with id = %d is not initiator of the event", userId));
        }

        List<Request> confirmedRequests = getConfirmedRequests(List.of(event));
        List<ViewStatsDto> stats = getViewsStats(List.of(event));

        return EventMapper.toEventFullDto(event, confirmedRequests, stats);
    }

    @Override
    @Transactional
    public EventFullDto updateEventById(long userId, long eventId, UpdateEventUserRequest updateDto) {
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

        Event updatedEvent = eventRepository.save(eventToUpdate);

        List<Request> confirmedRequests = getConfirmedRequests(List.of(updatedEvent));
        List<ViewStatsDto> stats = getViewsStats(List.of(updatedEvent));

        return EventMapper.toEventFullDto(updatedEvent, confirmedRequests, stats);
    }

    @Override
    public List<EventFullDto> getAllEvents(AdminEventRequestParams params, PageRequest pageRequest) {
        List<Event> events;

        if (Objects.nonNull(params.getPredicate())) {
            events = eventRepository.findAll(params.getPredicate(), pageRequest).getContent();
        } else {
            events = eventRepository.findAll(pageRequest).getContent();
        }

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Request> confirmedRequests = getConfirmedRequests(events);
        List<ViewStatsDto> stats = getViewsStats(events);

        return EventMapper.toEventFullDtoList(events, confirmedRequests, stats);
    }

    @Override
    @Transactional
    public EventFullDto updateEventById(long eventId, UpdateEventAdminRequest updateDto) {
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

        Event updatedEvent = eventRepository.save(eventToUpdate);

        List<Request> confirmedRequests = getConfirmedRequests(List.of(eventToUpdate));
        List<ViewStatsDto> stats = getViewsStats(List.of(eventToUpdate));

        return EventMapper.toEventFullDto(updatedEvent, confirmedRequests, stats);
    }

    @Override
    public List<EventShortDto> getAllEvents(PublicEventRequestParams params, PageRequest pageRequest) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null) {
            if (params.getRangeStart().isAfter(params.getRangeEnd())) {
                throw new EventDateException("Event end date can't be earlier than start date");
            }
        }

        BooleanBuilder builder = params.getBuilder();
        if (params.getOnlyAvailable() != null) {
            if (params.getOnlyAvailable()) {
                List<Long> availableEventIds = requestRepository.findEventIdsWithAvailableSpots();
                builder.and(QEvent.event.id.in(availableEventIds));
            }
        }

        assert builder.getValue() != null;
        List<Event> events = eventRepository.findAll(builder.getValue(), pageRequest.withSort(params.getSort())).getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<Request> confirmedRequests = getConfirmedRequests(events);
        List<ViewStatsDto> stats = getViewsStats(events);

        return EventMapper.toEventShortDtoList(events, confirmedRequests, stats);
    }

    @Override
    public EventFullDto getEventById(long eventId) {
        BooleanExpression conditions = QEvent.event.id.eq(eventId).and(QEvent.event.state.eq(EventState.PUBLISHED));

        Event event = eventRepository.findOne(conditions)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));

        // int confirmedRequestsCount = requestRepository.countByEventIdsAndStatus(eventId, RequestStatus.CONFIRMED);
        List<Request> confirmedRequests = getConfirmedRequests(List.of(event));
        List<ViewStatsDto> stats = getViewsStats(List.of(event));

        return EventMapper.toEventFullDto(event, confirmedRequests, stats);
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

    private List<ViewStatsDto> getViewsStats(List<Event> events) {
        String regexp = "/events/";
        List<String> uris = events.stream().map(Event::getId).map(id -> regexp + id).toList();
        LocalDateTime start = events.stream()
                .map(event -> event.getPublishedOn() != null ? event.getPublishedOn() : LocalDateTime.now().minusDays(7))
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(7));
        return statsClient.getStats(start, LocalDateTime.now(), uris, true);
    }

    private List<Request> getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        return requestRepository.findAllByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);
    }
}