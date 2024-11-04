package ru.practicum.ewm.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}")
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping("/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllRequests(@PathVariable long userId) {
        log.info("Main-service: received PRIVATE request to get all requests by user id: {}", userId);
        List<ParticipationRequestDto> requests = requestService.getAllRequests(userId);
        log.info("Main-service: requests received: {}", requests);
        return requests;
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable long userId,
            @RequestParam long eventId) {
        log.info("Main-service: received PRIVATE request to CREATE request by user id = {} to event id = {}", userId, eventId);
        ParticipationRequestDto createdRequest = requestService.createRequest(userId, eventId);
        log.info("Main-service: requests created: {}", createdRequest);
        return createdRequest;
    }

    @PatchMapping("/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(
            @PathVariable long userId,
            @PathVariable long requestId) {
        log.info("Main-service: received PRIVATE request to CANCEL request with id = {}", requestId);
        ParticipationRequestDto cancelledRequest = requestService.cancelRequest(userId, requestId);
        log.info("Main-service: request cancelled: {}", cancelledRequest);
        return cancelledRequest;
    }

    @GetMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable long eventId) {
        log.info("Main-service: received PRIVATE request to GET request by event id = {}", eventId);
        List<ParticipationRequestDto> requests = requestService.getRequestsByEventId(eventId);
        log.info("Main-service: requests received: {}", requests);
        return requests;
    }

    @PatchMapping("/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestsByEvent(
            @PathVariable long userId,
            @PathVariable long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequestDto
    ) {
        log.info("Main-service: received PRIVATE request to update request: {}", updateRequestDto);
        EventRequestStatusUpdateResult updatedRequest = requestService.updateRequestByEventId(userId, eventId, updateRequestDto);
        log.info("Main-service: request updated: {}", updatedRequest);
        return updatedRequest;
    }
}