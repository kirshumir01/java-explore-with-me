package ru.practicum.ewm.category.service;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto adminCreateCategory(NewCategoryDto createDto);

    void adminDeleteCategory(long catId);

    CategoryDto adminUpdateCategory(CategoryDto dto);

    List<CategoryDto> publicGetAllCategories(int from, int size);

    CategoryDto publicGetCategoryById(long catId);
}