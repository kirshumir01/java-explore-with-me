package ru.practicum.ewm.compilation.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.model.QCompilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto adminCreateCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = new HashSet<>();

        if (newCompilationDto.getEvents() != null) {
            events = newCompilationDto.getEvents().stream()
                    .map(eventId -> eventRepository.findById(eventId)
                            .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId))
                            ))
                    .collect(Collectors.toSet());
        }

        Compilation savedCompilation = compilationRepository
                .save(CompilationMapper.toCompilation(newCompilationDto, events));

        Set<EventShortDto> eventShortDtoSet = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toSet());

        return CompilationMapper.toCompilationDto(savedCompilation, eventShortDtoSet);
    }

    @Override
    @Transactional
    public void adminDeleteCompilation(long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException(String.format("Compilation with id = %d not found.", compId));
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto adminUpdateCompilation(long compId, UpdateCompilationRequest updateDto) {
        Compilation compilationToUpdate = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));

        if (updateDto.getEvents() != null) {
            Set<Event> events = updateDto.getEvents().stream()
                    .map(eventId -> eventRepository.findById(eventId)
                            .orElseThrow(() -> new NotFoundException(String.format("Event with id = %d not found", eventId))
                    ))
                    .collect(Collectors.toSet());
            compilationToUpdate.setEvents(events);
        }
        if (updateDto.getTitle() != null && !updateDto.getTitle().isBlank()) {
            compilationToUpdate.setTitle(updateDto.getTitle());
        }
        if (updateDto.getPinned() != null) {
            compilationToUpdate.setPinned(updateDto.getPinned());
        }

        Compilation updatedCompilation = compilationRepository.save(compilationToUpdate);

        Set<EventShortDto> eventShortDtoSet = updatedCompilation.getEvents().stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toSet());

        return CompilationMapper.toCompilationDto(updatedCompilation, eventShortDtoSet);
    }

    @Override
    public List<CompilationDto> publicGetAllCompilations(Boolean pinned, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);

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

        return compilations.stream()
                .map(compilation -> {
                    Set<EventShortDto> eventShortDtoSet = compilation.getEvents().stream()
                            .map(EventMapper::toEventShortDto)
                            .collect(Collectors.toSet());
                    return CompilationMapper.toCompilationDto(compilation, eventShortDtoSet);
                }).toList();
    }

    @Override
    public CompilationDto publicGetCompilationById(long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id = %d not found", compId)));

        Set<EventShortDto> eventShortDtoSet = compilation.getEvents().stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toSet());

        return CompilationMapper.toCompilationDto(compilation, eventShortDtoSet);
    }
}