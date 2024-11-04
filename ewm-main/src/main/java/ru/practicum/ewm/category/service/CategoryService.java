package ru.practicum.ewm.category.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto createDto);

    void deleteCategory(long catId);

    CategoryDto updateCategory(CategoryDto dto);

    List<CategoryDto> getAllCategories(Pageable page);

    CategoryDto getCategoryById(long catId);
}