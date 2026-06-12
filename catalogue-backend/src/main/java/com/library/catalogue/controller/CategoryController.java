package com.library.catalogue.controller;

import com.library.catalogue.config.CachingConfig;
import com.library.catalogue.dto.BookResponseDto;
import com.library.catalogue.dto.CategoryRequestDto;
import com.library.catalogue.dto.CategoryResponseDto;
import com.library.catalogue.service.BookService;
import com.library.catalogue.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories(
            @RequestParam(defaultValue = "false") boolean flat) {
        if (flat) {
            return ResponseEntity.ok()
                    .cacheControl(CachingConfig.categoriesCache())
                    .body(categoryService.getFlatCategories());
        }
        return ResponseEntity.ok()
                .cacheControl(CachingConfig.categoriesCache())
                .body(categoryService.getCategoryTree());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<Page<BookResponseDto>> getCategoryBooks(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        int pageIndex = Math.max(page - 1, 0);
        return ResponseEntity.ok(bookService.getBooksByCategory(id, PageRequest.of(pageIndex, size)));
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(requestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
