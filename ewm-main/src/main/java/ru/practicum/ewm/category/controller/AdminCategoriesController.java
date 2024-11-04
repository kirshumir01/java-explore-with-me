package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoriesController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Main-service: received ADMIN request to CREATE category: {}", newCategoryDto);
        CategoryDto createCategory = categoryService.createCategory(newCategoryDto);
        log.info("Main-service: category was created: {}", createCategory);
        return createCategory;
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long catId) {
        log.info("Main-service: received ADMIN request to DELETE category with id: {}", catId);
        categoryService.deleteCategory(catId);
        log.info("Main-service: category was deleted");
    }

    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable long catId,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Main-service: received ADMIN request to UPDATE category: {}", categoryDto);
        categoryDto.setId(catId);
        CategoryDto updatedCategory = categoryService.updateCategory(categoryDto);
        log.info("Main-service: category was updated");
        return updatedCategory;
    }
}