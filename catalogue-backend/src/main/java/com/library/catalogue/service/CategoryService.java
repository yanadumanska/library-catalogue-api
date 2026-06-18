package com.library.catalogue.service;

import com.library.catalogue.dto.CategoryRequestDto;
import com.library.catalogue.dto.CategoryResponseDto;
import com.library.catalogue.entity.CategoryEntity;
import com.library.catalogue.exception.CategoryNotFoundException;
import com.library.catalogue.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "categoryTree")
    public List<CategoryResponseDto> getCategoryTree() {
        List<CategoryEntity> roots = categoryRepository.findRootCategoriesWithChildren();
        return roots.stream()
                .map(this::mapToTreeDto)
                .toList();
    }

    @Cacheable(value = "categoryFlat")
    public List<CategoryResponseDto> getFlatCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToFlatDto)
                .toList();
    }

    @Cacheable(value = "category", key = "#id")
    public CategoryResponseDto getCategoryById(UUID id) {
        CategoryEntity category = getCategoryEntityById(id);
        return mapToTreeDto(category);
    }

    public CategoryEntity getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    @CacheEvict(value = {"categoryTree", "categoryFlat", "category"}, allEntries = true)
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        CategoryEntity parent = requestDto.getParentCategoryId() != null
                ? getCategoryEntityById(requestDto.getParentCategoryId())
                : null;

        CategoryEntity category = CategoryEntity.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .parentCategory(parent)
                .build();

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Created category: {}", saved.getName());
        return mapToFlatDto(saved);
    }

    @Transactional
    @CacheEvict(value = {"categoryTree", "categoryFlat", "category"}, allEntries = true)
    public CategoryResponseDto updateCategory(UUID id, CategoryRequestDto requestDto) {
        CategoryEntity category = getCategoryEntityById(id);

        category.setName(requestDto.getName());
        category.setDescription(requestDto.getDescription());
        category.setParentCategory(requestDto.getParentCategoryId() != null
                ? getCategoryEntityById(requestDto.getParentCategoryId())
                : null);

        CategoryEntity updated = categoryRepository.save(category);
        log.info("Updated category: {}", updated.getName());
        return mapToFlatDto(updated);
    }

    @Transactional
    @CacheEvict(value = {"categoryTree", "categoryFlat", "category"}, allEntries = true)
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
                .parentCategoryId(category.getParentCategory() != null
                        ? category.getParentCategory().getId() : null)
                .subcategories(category.getSubcategories() != null
                        ? category.getSubcategories().stream()
                        .map(this::mapToTreeDto)
                        .toList()
                        : null)
                .build();
    }

    private CategoryResponseDto mapToFlatDto(CategoryEntity cat) {
        return CategoryResponseDto.builder()
                .id(cat.getId())
                .name(cat.getName())
                .description(cat.getDescription())
                .parentCategoryId(cat.getParentCategory() != null
                        ? cat.getParentCategory().getId() : null)
                .subcategories(null)
                .build();
    }
}
