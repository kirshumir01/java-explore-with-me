package ru.practicum.ewm.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto createDto) {
        log.info("Main-service: received ADMIN request to CREATE compilation: {}", createDto);
        CompilationDto createdCompilation = compilationService.createCompilation(createDto);
        log.info("Main-service: compilation created: {}", createdCompilation);
        return createdCompilation;
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable(name = "compId") long compId) {
        log.info("Main-service: received ADMIN request to DELETE compilation with id = {}", compId);
        compilationService.deleteCompilation(compId);
        log.info("Main-service: compilation deleted");
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@PathVariable(name = "compId") long compId,
                                             @Valid @RequestBody UpdateCompilationRequest updateDto) {
        log.info("Main-service: received ADMIN request to UPDATE compilation: {}", updateDto);
        CompilationDto updatedCompilation = compilationService.updateCompilation(compId, updateDto);
        log.info("Main-service: compilation updated: {}", updatedCompilation);
        return updatedCompilation;
    }
}