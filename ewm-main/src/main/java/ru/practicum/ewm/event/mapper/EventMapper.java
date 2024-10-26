package ru.practicum.ewm.event.mapper;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.exception.URIFormatException;
import ru.practicum.ewm.location.mapper.LocationMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventMapper {
    public static EventShortDto toEventShortDto(Event event,
                                                List<Request> requests,
                                                List<ViewStatsDto> stats) {
        Long views = getEventViewsStatistic(List.of(event), stats).get(event.getId());
        Integer confirmedRequests;

        if (requests == null || requests.isEmpty()) {
            confirmedRequests = 0;
        } else {
            confirmedRequests = getConfirmedRequests(requests).get(event.getId());
        }

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder().id(event.getCategory().getId()).build())
                .confirmedRequests(confirmedRequests)
                .eventDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(event.getEventDate()))
                .initiator(UserShortDto.builder().id(event.getInitiator().getId()).build())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static List<EventShortDto> toEventShortDtoList(List<Event> events,
                                      List<Request> confirmedRequests,
                                      List<ViewStatsDto> stats) {
        return events.stream()
                .map(event -> {
                    return toEventShortDto(event, confirmedRequests, stats);
        }).toList();
    }

    public static EventFullDto toEventFullDto(Event event,
                                              List<Request> confirmedRequests,
                                              List<ViewStatsDto> stats) {
        EventShortDto eventShortDto = toEventShortDto(event, confirmedRequests, stats);

        String publishedOn;
        if (event.getPublishedOn() != null) {
            publishedOn = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(event.getPublishedOn());
        } else {
            publishedOn = null;
        }

        return new EventFullDto(
                eventShortDto,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(event.getCreatedOn()),
                event.getDescription(),
                LocationMapper.toLocationDto(event.getLocation()),
                event.getParticipantLimit(),
                publishedOn,
                event.getRequestModeration(),
                event.getState()
        );
    }

    public static List<EventFullDto> toEventFullDtoList(List<Event> events,
                                                        List<Request> confirmedRequests,
                                                        List<ViewStatsDto> stats) {
        return events.stream()
                .map(event -> {
                    return toEventFullDto(event, confirmedRequests, stats);
        }).toList();

    }

    public static Event toEvent(NewEventDto newEventDto) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(LocationMapper.toLocation(newEventDto.getLocation()))
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }

    private static Map<Long, Long> getEventViewsStatistic(List<Event> events, List<ViewStatsDto> stats) {
        String regexp = "/events/";
        Map<Long, Long> viewsMap = new HashMap<>();

        for (ViewStatsDto stat : stats) {
            String uri = stat.getUri();
            if (uri.startsWith(regexp)) {
                try {
                    long eventId = Long.parseLong(uri.substring(regexp.length()));
                    viewsMap.put(eventId, stat.getHits());
                } catch (URIFormatException e) {
                    throw new URIFormatException("Illegal character in path: " + uri);
                }
            }
        }

        events.stream()
                .map(Event::getId)
                .forEach(id -> viewsMap.putIfAbsent(id, 0L));

        return viewsMap;
    }

    private static Map<Long, Integer> getConfirmedRequests(List<Request> confirmedRequests) {
        return confirmedRequests.stream()
                .collect(Collectors.groupingBy(
                        request -> request.getEvent().getId(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }
}
