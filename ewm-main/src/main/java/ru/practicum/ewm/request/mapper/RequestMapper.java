package ru.practicum.ewm.request.mapper;

import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.model.User;

import java.time.format.DateTimeFormatter;

public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(Request participation) {
        return ParticipationRequestDto.builder()
                .id(participation.getId())
                .created(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(participation.getCreated()))
                .event(participation.getEvent().getId())
                .requester(participation.getUser().getId())
                .status(participation.getStatus().toString())
                .build();
    }

    public static Request toRequest(Event event, User requester) {
        RequestStatus requestStatus;

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            requestStatus = RequestStatus.CONFIRMED;
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            requestStatus = RequestStatus.PENDING;
        }

        return Request.builder()
                .event(event)
                .user(requester)
                .status(requestStatus)
                .build();
    }
}
