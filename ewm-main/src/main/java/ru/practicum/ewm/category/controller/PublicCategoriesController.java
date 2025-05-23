package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class PublicCategoriesController {
    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getAllCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Main-service: received PUBLIC request to GET all categories");
        Pageable page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CategoryDto> categoryDtoList = categoryService.getAllCategories(page);
        log.info("Main-service: categories received: {}", categoryDtoList);
        return categoryDtoList;

    }

    @GetMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategory(@PathVariable(name = "catId") long catId) {
        log.info("Main-service: received PUBLIC request to GET category with id: {}", catId);
        CategoryDto receivedCategory = categoryService.getCategoryById(catId);
        log.info("Main-service: category received: {}", receivedCategory);
        return receivedCategory;
    }
}