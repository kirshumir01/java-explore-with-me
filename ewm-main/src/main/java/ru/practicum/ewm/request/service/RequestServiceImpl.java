package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> privateGetAllRequests(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }
        return requestRepository.findAllByUserId(userId).stream()
                .map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto privateCreateRequest(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId)));
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id = %d not found", userId)));

        validateEventToParticipate(event, requester);

        Request request = RequestMapper.toRequest(event, requester);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        eventRepository.save(event);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto privateCancelRequest(long userId, long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }

        Request request = requestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id = %d not found", requestId)));
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> privateGetRequestsByEventId(long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException(String.format("Event with id = %d not found", eventId));
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);

        if (requests.isEmpty()) {
            return List.of();
        }
        return requests.stream().map(RequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult privateUpdateRequestByEventId(
            long userId,
            long eventId,
            EventRequestStatusUpdateRequest updateRequestDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id = %d not found.", eventId)));

        List<Request> requests = requestRepository.findByIdIn(updateRequestDto.getRequestIds());

        boolean isConfirmedRequest = requests.stream()
                .anyMatch(request -> request.getStatus().equals(RequestStatus.CONFIRMED));

        boolean isPendingRequests = requests.stream()
                .anyMatch(request -> request.getStatus().equals(RequestStatus.PENDING));

        if (isConfirmedRequest && updateRequestDto.getStatus().equals(EventRequestStatusUpdateRequest.RequestStatus.REJECTED)) {
            throw new ConflictException("Confirmed request couldn't be reject");
        }

        if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConflictException("The limit of participants has been reached");
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            requests.forEach(request -> request.setStatus(RequestStatus.CONFIRMED));
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(requests.stream().map(RequestMapper::toParticipationRequestDto).toList())
                    .build();
        }

        if (updateRequestDto.getStatus().equals(EventRequestStatusUpdateRequest.RequestStatus.REJECTED)) {
            requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            return EventRequestStatusUpdateResult.builder()
                    .rejectedRequests(requests.stream().map(RequestMapper::toParticipationRequestDto).toList())
                    .build();
        }

        if (!isPendingRequests) {
            throw new ConflictException("The status can only be changed for applications that are in a pending state.");
        }

        RequestStatus requestStatusToUpdate = RequestStatus.valueOf(updateRequestDto.getStatus().name());
        int participantLimit = event.getParticipantLimit();
        int confirmedRequests = event.getConfirmedRequests();
        List<Request> confirmedRequestsList = new ArrayList<>();
        List<Request> rejectedRequestsList = new ArrayList<>();

        for (Request request : requests) {
            if (confirmedRequests == participantLimit) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequestsList.add(request);
            } else {
                request.setStatus(requestStatusToUpdate);
                confirmedRequestsList.add(request);
                confirmedRequests++;
            }
        }

        event.setConfirmedRequests(confirmedRequests);
        requestRepository.saveAll(requests);
        eventRepository.save(event);
        return EventRequestStatusUpdateResult.builder()
                .rejectedRequests(rejectedRequestsList.stream().map(RequestMapper::toParticipationRequestDto).toList())
                .confirmedRequests(confirmedRequestsList.stream().map(RequestMapper::toParticipationRequestDto).toList())
                .build();
    }

    private void validateEventToParticipate(Event event, User requester) {
        if (Objects.equals(event.getInitiator().getId(), requester.getId())) {
            throw new ConflictException("Initiator can't submit a request to participate in own event");
        }
        if (!requestRepository.findByEventAndUser(event, requester).isEmpty()) {
            throw new ConflictException("The request for participation has already been submitted");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Request couldn't be submit in an unpublished event");
        }
        if (event.getParticipantLimit() != 0)
            if (requestRepository.isParticipantLimitReached(event.getId(), event.getParticipantLimit())) {
            throw new ConflictException("The limit of requests has been reached");
        }
    }
}