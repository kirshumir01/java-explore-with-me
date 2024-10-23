package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> privateGetAllRequests(long userId);

    ParticipationRequestDto privateCreateRequest(long userId, long eventId);

    ParticipationRequestDto privateCancelRequest(long userId, long requestId);

    List<ParticipationRequestDto> privateGetRequestsByEventId(long eventId);

    EventRequestStatusUpdateResult privateUpdateRequestByEventId(long userId, long eventId, EventRequestStatusUpdateRequest updateDto);
}