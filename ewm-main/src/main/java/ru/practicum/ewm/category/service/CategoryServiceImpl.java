package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Set<String> categoryNames = categoryRepository.findAll().stream().map(Category::getName).collect(Collectors.toSet());

        if (categoryNames.contains(newCategoryDto.getName())) {
            throw new ConflictException(String.format("Category with same name '%s' exists", newCategoryDto.getName()));
        }

        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException(String.format("Category with id = %d not found", catId));
        }

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException(String.format("Category with id = %d contains events", catId));
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto updateDto) {
        Category categoryToUpdate = categoryRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException(String.format("Category with id = %d not found", updateDto.getId())));

        List<String> categoryNames = categoryRepository.findAll().stream().map(Category::getName).toList();

        if (categoryNames.contains(updateDto.getName())
                && !categoryToUpdate.getName().equals(updateDto.getName())) {
            throw new ConflictException(String.format("Category with same name '%s' exists", updateDto.getName()));
        }

        categoryToUpdate.setName(updateDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryToUpdate));
    }

    @Override
    public List<CategoryDto> getAllCategories(Pageable page) {
        List<Category> categories = categoryRepository.findAll(page).getContent();

        if (categories.isEmpty()) {
            return Collections.emptyList();
        }

        return categories.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id = %d not found", catId)));
        return CategoryMapper.toCategoryDto(category);
    }
}