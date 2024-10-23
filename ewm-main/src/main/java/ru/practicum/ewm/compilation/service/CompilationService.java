package ru.practicum.ewm.compilation.service;

import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto adminCreateCompilation(NewCompilationDto createDto);

    void adminDeleteCompilation(long compId);

    CompilationDto adminUpdateCompilation(long compId, UpdateCompilationRequest updateDto);

    List<CompilationDto> publicGetAllCompilations(Boolean pinned, int from, int size);

    CompilationDto publicGetCompilationById(long compId);
}