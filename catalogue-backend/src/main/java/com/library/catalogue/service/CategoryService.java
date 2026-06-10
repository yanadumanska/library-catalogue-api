package com.library.catalogue.service;

import com.library.catalogue.dto.CategoryRequestDto;
import com.library.catalogue.dto.CategoryResponseDto;
import com.library.catalogue.entity.CategoryEntity;
import com.library.catalogue.exception.CategoryNotFoundException;
import com.library.catalogue.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDto> getCategoryTree() {
        List<CategoryEntity> roots = categoryRepository.findRootCategoriesWithChildren();
        return roots.stream()
                .map(this::mapToTreeDto)
                .toList();
    }

    public List<CategoryResponseDto> getFlatCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> CategoryResponseDto.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .description(cat.getDescription())
                        .parentCategoryId(cat.getParentCategory() != null ? cat.getParentCategory().getId() : null)
                        .subcategories(null)
                        .build())
                .toList();
    }

    public CategoryResponseDto getCategoryById(UUID id) {
        CategoryEntity category = getCategoryEntityById(id);
        return mapToTreeDto(category);
    }

    public CategoryEntity getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        CategoryEntity parent = null;
        if (requestDto.getParentCategoryId() != null) {
            parent = getCategoryEntityById(requestDto.getParentCategoryId());
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .parentCategory(parent)
                .build();

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Created category: {}", saved.getName());
        return mapToDto(saved);
    }

    @Transactional
    public CategoryResponseDto updateCategory(UUID id, CategoryRequestDto requestDto) {
        CategoryEntity category = getCategoryEntityById(id);

        category.setName(requestDto.getName());
        category.setDescription(requestDto.getDescription());

        if (requestDto.getParentCategoryId() != null) {
            CategoryEntity parent = getCategoryEntityById(requestDto.getParentCategoryId());
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        CategoryEntity updated = categoryRepository.save(category);
        log.info("Updated category: {}", updated.getName());
        return mapToDto(updated);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        CategoryEntity category = getCategoryEntityById(id);
        categoryRepository.delete(category);
        log.info("Deleted category: {}", category.getName());
    }

    private CategoryResponseDto mapToTreeDto(CategoryEntity category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .subcategories(category.getSubcategories() != null ?
                        category.getSubcategories().stream()
                                .map(this::mapToTreeDto)
                                .toList() : null)
                .build();
    }

    private CategoryResponseDto mapToDto(CategoryEntity category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .build();
    }
}
