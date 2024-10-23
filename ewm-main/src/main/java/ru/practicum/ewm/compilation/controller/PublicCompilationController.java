package ru.practicum.ewm.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getAllCompilations(
            @RequestParam(required = false) boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Main-service: received PUBLIC request to GET all compilations with 'pinned' = '{}'", pinned);
        List<CompilationDto> compilations = compilationService.publicGetAllCompilations(pinned, from, size);
        log.info("Main-service: compilations received: {}", compilations);
        return compilations;
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilation(@PathVariable long compId) {
        log.info("Main-service: received PUBLIC request to GET compilation with id = {}", compId);
        CompilationDto compilation = compilationService.publicGetCompilationById(compId);
        log.info("Main-service: compilation received: {}", compilation);
        return compilation;
    }
}