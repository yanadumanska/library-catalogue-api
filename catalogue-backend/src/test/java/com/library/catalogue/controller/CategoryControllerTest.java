package com.library.catalogue.controller;

import com.library.catalogue.dto.BookResponseDto;
import com.library.catalogue.dto.CategoryRequestDto;
import com.library.catalogue.dto.CategoryResponseDto;
import com.library.catalogue.service.BookService;
import com.library.catalogue.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private CategoryController categoryController;

    private UUID categoryId;
    private CategoryResponseDto categoryDto;
    private CategoryRequestDto categoryRequest;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        categoryDto = CategoryResponseDto.builder()
                .id(categoryId)
                .name("Fiction")
                .description("Fiction books")
                .subcategories(List.of())
                .build();

        categoryRequest = CategoryRequestDto.builder()
                .name("Fiction")
                .description("Fiction books")
                .build();
    }

    @Test
    void getCategories_Tree_ShouldReturnTree() {
        when(categoryService.getCategoryTree()).thenReturn(List.of(categoryDto));

        ResponseEntity<List<CategoryResponseDto>> response = categoryController.getCategories(false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(categoryService).getCategoryTree();
        verify(categoryService, never()).getFlatCategories();
    }

    @Test
    void getCategories_Flat_ShouldReturnFlat() {
        when(categoryService.getFlatCategories()).thenReturn(List.of(categoryDto));

        ResponseEntity<List<CategoryResponseDto>> response = categoryController.getCategories(true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(categoryService).getFlatCategories();
        verify(categoryService, never()).getCategoryTree();
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        when(categoryService.getCategoryById(categoryId)).thenReturn(categoryDto);

        ResponseEntity<CategoryResponseDto> response = categoryController.getCategoryById(categoryId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fiction", response.getBody().getName());
    }

    @Test
    void getCategoryBooks_ShouldReturnBooks() {
        BookResponseDto bookDto = BookResponseDto.builder()
                .id(UUID.randomUUID())
                .title("1984")
                .build();
        Page<BookResponseDto> page = new PageImpl<>(List.of(bookDto));
        when(bookService.getBooksByCategory(eq(categoryId), any())).thenReturn(page);

        ResponseEntity<Page<BookResponseDto>> response = categoryController.getCategoryBooks(categoryId, 1, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getCategoryBooks_PageIndex_ShouldBeZeroBased() {
        Page<BookResponseDto> page = new PageImpl<>(List.of());
        when(bookService.getBooksByCategory(eq(categoryId), any())).thenReturn(page);

        categoryController.getCategoryBooks(categoryId, 1, 20);

        verify(bookService).getBooksByCategory(eq(categoryId), argThat(p -> p.getPageNumber() == 0));
    }

    @Test
    void createCategory_ShouldReturnCreated() {
        when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(categoryDto);

        ResponseEntity<CategoryResponseDto> response = categoryController.createCategory(categoryRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Fiction", response.getBody().getName());
    }

    @Test
    void updateCategory_ShouldReturnOk() {
        when(categoryService.updateCategory(eq(categoryId), any(CategoryRequestDto.class))).thenReturn(categoryDto);

        ResponseEntity<CategoryResponseDto> response = categoryController.updateCategory(categoryId, categoryRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fiction", response.getBody().getName());
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() {
        doNothing().when(categoryService).deleteCategory(categoryId);

        ResponseEntity<Void> response = categoryController.deleteCategory(categoryId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(categoryService).deleteCategory(categoryId);
    }
}
