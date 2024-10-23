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
public class UpdateEventAdminRequest extends UpdateEventDto {
    @JsonProperty(value = "stateAction")
    private StateActionAdmin stateActionAdmin;

    public enum StateActionAdmin {
        PUBLISH_EVENT, REJECT_EVENT
    }
}