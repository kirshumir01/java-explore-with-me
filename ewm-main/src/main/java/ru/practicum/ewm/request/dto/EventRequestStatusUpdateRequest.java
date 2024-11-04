package ru.practicum.ewm.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    private Set<Long> requestIds;
    @JsonProperty(value = "status")
    private RequestStatus status;

    public enum RequestStatus {
        CONFIRMED, REJECTED
    }
}