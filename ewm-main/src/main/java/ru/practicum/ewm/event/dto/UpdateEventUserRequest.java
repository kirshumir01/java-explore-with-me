package ru.practicum.ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UpdateEventUserRequest extends UpdateEventDto {
    @JsonProperty(value = "stateAction")
    private StateActionUser stateActionUser;

    public enum StateActionUser {
        SEND_TO_REVIEW, CANCEL_REVIEW
    }
}