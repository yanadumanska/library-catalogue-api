package com.library.catalogue.controller;

import com.library.catalogue.dto.AuthorResponseDto;
import com.library.catalogue.dto.BookRequestDto;
import com.library.catalogue.dto.BookResponseDto;
import com.library.catalogue.dto.CategoryResponseDto;
import com.library.catalogue.enums.BookFormat;
import com.library.catalogue.enums.BookStatus;
import com.library.catalogue.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private UUID bookId;
    private BookResponseDto bookDto;
    private BookRequestDto bookRequest;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();

        AuthorResponseDto authorDto = AuthorResponseDto.builder()
                .id(UUID.randomUUID())
                .firstName("Robert")
                .lastName("Martin")
                .build();

        CategoryResponseDto categoryDto = CategoryResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Programming")
                .build();

        bookDto = BookResponseDto.builder()
                .id(bookId)
                .isbn("9780132350884")
                .title("Clean Code")
                .format(BookFormat.PAPERBACK)
                .status(BookStatus.AVAILABLE)
                .availableCopies(3)
                .totalCopies(5)
                .averageRating(BigDecimal.valueOf(4.5))
                .authors(List.of(authorDto))
                .categories(List.of(categoryDto))
                .build();

        bookRequest = BookRequestDto.builder()
                .isbn("9780132350884")
                .title("Clean Code")
                .format(BookFormat.PAPERBACK)
                .totalCopies(5)
                .availableCopies(3)
                .build();
    }

    @Test
    void getAllBooks_ShouldReturnPage() {
        Page<BookResponseDto> page = new PageImpl<>(List.of(bookDto));
        when(bookService.getBooksWithFilters(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(page);

        ResponseEntity<Page<BookResponseDto>> response = bookController.getAllBooks(
                1, 20, "title:asc", null, null, null, null, null,
                null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getAllBooks_PageIndex_ShouldBeZeroBased() {
        Page<BookResponseDto> page = new PageImpl<>(List.of());
        when(bookService.getBooksWithFilters(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(page);

        bookController.getAllBooks(1, 20, "title:asc", null, null, null, null, null,
                null, null, null, null);

        verify(bookService).getBooksWithFilters(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), argThat(p -> p.getPageNumber() == 0));
    }

    @Test
    void getAllBooks_WithFilters_ShouldPassParameters() {
        Page<BookResponseDto> page = new PageImpl<>(List.of());
        when(bookService.getBooksWithFilters(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(page);

        bookController.getAllBooks(1, 10, "title:desc", "code", "Martin", "Java",
                BookFormat.PAPERBACK, BookStatus.AVAILABLE, "en", BigDecimal.valueOf(4),
                null, null);

        verify(bookService).getBooksWithFilters(
                eq("code"), eq(BookFormat.PAPERBACK), eq(BookStatus.AVAILABLE), eq("en"),
                eq(BigDecimal.valueOf(4)), isNull(), isNull(), eq("Martin"), eq("Java"),
                any());
    }

    @Test
    void getBookById_ShouldReturnBook() {
        when(bookService.getBookById(bookId)).thenReturn(bookDto);

        ResponseEntity<BookResponseDto> response = bookController.getBookById(bookId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Clean Code", response.getBody().getTitle());
    }

    @Test
    void getBookAuthors_ShouldReturnAuthors() {
        when(bookService.getBookById(bookId)).thenReturn(bookDto);

        ResponseEntity<List<AuthorResponseDto>> response = bookController.getBookAuthors(bookId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Robert", response.getBody().get(0).getFirstName());
    }

    @Test
    void getBookCategories_ShouldReturnCategories() {
        when(bookService.getBookById(bookId)).thenReturn(bookDto);

        ResponseEntity<List<CategoryResponseDto>> response = bookController.getBookCategories(bookId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Programming", response.getBody().get(0).getName());
    }

    @Test
    void createBook_ShouldReturnCreated() {
        when(bookService.createBook(any(BookRequestDto.class))).thenReturn(bookDto);

        ResponseEntity<BookResponseDto> response = bookController.createBook(bookRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Clean Code", response.getBody().getTitle());
    }

    @Test
    void updateBook_ShouldReturnOk() {
        when(bookService.updateBook(eq(bookId), any(BookRequestDto.class))).thenReturn(bookDto);

        ResponseEntity<BookResponseDto> response = bookController.updateBook(bookId, bookRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5, response.getBody().getTotalCopies());
    }

    @Test
    void deleteBook_ShouldReturnNoContent() {
        doNothing().when(bookService).deleteBook(bookId);

        ResponseEntity<Void> response = bookController.deleteBook(bookId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(bookService).deleteBook(bookId);
    }

    @Test
    void isBookAvailable_ShouldReturnTrue() {
        when(bookService.isBookAvailable(bookId)).thenReturn(true);

        ResponseEntity<Boolean> response = bookController.isBookAvailable(bookId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }
}
