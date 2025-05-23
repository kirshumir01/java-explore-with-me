package ru.practicum.ewm.compilation.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.StatsClient;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.QCompilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.RequestCount;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = new HashSet<>();
        List<RequestCount> confirmedRequests = new ArrayList<>();
        List<ViewStatsDto> stats = new ArrayList<>();
        Set<EventShortDto> eventShortDtoSet = new HashSet<>();

        if (newCompilationDto.getEvents() != null) {
            Set<Long> eventsIds = newCompilationDto.getEvents();
            events = new HashSet<>(eventRepository.findAllById(eventsIds));
            confirmedRequests = getConfirmedRequests(events.stream().toList());
            stats = getViewsStats(events.stream().toList());
            eventShortDtoSet = new HashSet<>(EventMapper.toEventShortDtoList(events.stream().toList(), confirmedRequests, stats));
        }

        Compilation savedCompilation = compilationRepository
                .save(CompilationMapper.toCompilation(newCompilationDto, events));

        return CompilationMapper.toCompilationDto(savedCompilation, eventShortDtoSet);
    }

    @Override
    @Transactional
    public void deleteCompilation(long compId) {
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateDto) {
        Compilation compilationToUpdate = compilationRepository.findByIdWithEvents(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));

        if (updateDto.getEvents() != null) {
            Set<Long> eventsIds = updateDto.getEvents();
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(eventsIds));
            compilationToUpdate.setEvents(events);
        }
        if (updateDto.getTitle() != null && !updateDto.getTitle().isBlank()) {
            compilationToUpdate.setTitle(updateDto.getTitle());
        }
        if (updateDto.getPinned() != null) {
            compilationToUpdate.setPinned(updateDto.getPinned());
        }

        Compilation updatedCompilation = compilationRepository.save(compilationToUpdate);

        List<Event> events = updatedCompilation.getEvents().stream().toList();
        List<RequestCount> confirmedRequests = getConfirmedRequests(events.stream().toList());
        List<ViewStatsDto> stats = getViewsStats(events);

        Set<EventShortDto> eventShortDtoSet = new HashSet<>(EventMapper.toEventShortDtoList(events, confirmedRequests, stats));

        return CompilationMapper.toCompilationDto(updatedCompilation, eventShortDtoSet);
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, PageRequest pageRequest) {
        List<Compilation> compilations;

        BooleanExpression conditions = QCompilation.compilation.isNotNull();

        if (pinned != null) {
            conditions = conditions.and(QCompilation.compilation.pinned.eq(pinned));
            compilations = compilationRepository.findAll(conditions, pageRequest).getContent();
        } else {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        }

        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> results = compilationRepository.findAllCompilationEventPairs();

        Map<Long, List<Long>> eventsIdByCompilationId = results.stream()
                .collect(Collectors.groupingBy(
                        row -> (Long.parseLong(row[0].toString())),
                        Collectors.mapping(
                                row -> (Long.parseLong(row[1].toString())),
                                Collectors.toList()
                        )
                ));

        Set<Long> allEventsIds = eventsIdByCompilationId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        List<Event> events = eventRepository.findAllByIdIn(allEventsIds);

        Map<Long, Set<Event>> eventsByCompilation = events.stream()
                .collect(Collectors.groupingBy(
                        event -> findCompilationIdForEvent(event.getId(), eventsIdByCompilationId),
                        Collectors.toSet()
                ));

        return  compilations.stream()
                .map(compilation -> {
                    Set<EventShortDto> eventShortDtoSet = new HashSet<>();
                    if (!eventsByCompilation.containsKey(compilation.getId())) {
                        return CompilationMapper.toCompilationDto(compilation, eventShortDtoSet);
                    }
                    eventShortDtoSet = getEventShortDtoSet(compilation);
                    return CompilationMapper.toCompilationDto(compilation, eventShortDtoSet);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(long compId) {
        Compilation compilation = compilationRepository.findByIdWithEvents(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));

        List<Event> events = compilation.getEvents().stream().toList();
        List<RequestCount> confirmedRequests = getConfirmedRequests(events.stream().toList());
        List<ViewStatsDto> stats = getViewsStats(events);

        Set<EventShortDto> eventShortDtoSet = new HashSet<>(EventMapper.toEventShortDtoList(events, confirmedRequests, stats));

        return CompilationMapper.toCompilationDto(compilation, eventShortDtoSet);
    }

    private List<ViewStatsDto> getViewsStats(List<Event> events) {
        String regexp = "/events/";
        List<String> uris = events.stream().map(Event::getId).map(id -> regexp + id).toList();
        LocalDateTime start = events.stream()
                .map(event -> event.getPublishedOn() != null ? event.getPublishedOn() : LocalDateTime.now().minusDays(7))
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(7));
        return statsClient.getStats(start, LocalDateTime.now(), uris, true);
    }

    private List<RequestCount> getConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        List<Request> confirmedRequests = requestRepository.findAllByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> requestCountMap = confirmedRequests.stream()
                .collect(Collectors.groupingBy(
                        request -> request.getEvent().getId(),
                        Collectors.counting()
                ));

        return requestCountMap.entrySet().stream()
                .map(entry -> new RequestCount(entry.getKey(), entry.getValue().intValue()))
                .toList();
    }

    private Long findCompilationIdForEvent(long eventId, Map<Long, List<Long>> eventsIdByCompilationId) {
        return eventsIdByCompilationId.entrySet().stream()
                .filter(entry -> entry.getValue().contains(eventId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private Set<EventShortDto> getEventShortDtoSet(Compilation compilation) {
        List<Event> events = compilation.getEvents().stream().toList();
        List<RequestCount> confirmedRequests = getConfirmedRequests(events);
        List<ViewStatsDto> stats = getViewsStats(events);
        List<EventShortDto> eventShortDtoList = EventMapper.toEventShortDtoList(events, confirmedRequests, stats);
        return new HashSet<>(eventShortDtoList);
    }
}