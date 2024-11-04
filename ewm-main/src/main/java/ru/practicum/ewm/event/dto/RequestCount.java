package ru.practicum.ewm.event.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCount {
    private Long eventId;
    private Integer count;

    public RequestCount(long eventId, int count) {
        this.eventId = eventId;
        this.count = count;
    }
}