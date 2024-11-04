package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.location.dto.LocationDto;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto extends EventShortDto {
    private String createdOn;
    private String description;
    @NotNull
    private LocationDto location;
    private Integer participantLimit;
    private String publishedOn;
    private Boolean requestModeration;
    private EventState state;

    public EventFullDto(EventShortDto eventShortDto, String createdOn, String description,
                        LocationDto location, Integer participantLimit, String publishedOn,
                        Boolean requestModeration, EventState state) {
        super(eventShortDto.getId(),
              eventShortDto.getAnnotation(),
              eventShortDto.getCategory(),
              eventShortDto.getConfirmedRequests(),
              eventShortDto.getEventDate(),
              eventShortDto.getInitiator(),
              eventShortDto.getPaid(),
              eventShortDto.getTitle(),
              eventShortDto.getViews()
        );
        this.createdOn = createdOn;
        this.description = description;
        this.location = location;
        this.participantLimit = participantLimit;
        this.publishedOn = publishedOn;
        this.requestModeration = requestModeration;
        this.state = state;
    }
}