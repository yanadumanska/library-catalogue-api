package com.library.catalogue.controller;

import com.library.catalogue.dto.AuthorRequestDto;
import com.library.catalogue.dto.AuthorResponseDto;
import com.library.catalogue.dto.BookResponseDto;
import com.library.catalogue.service.AuthorService;
import com.library.catalogue.service.BookService;
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
class AuthorControllerTest {

    @Mock
    private AuthorService authorService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private AuthorController authorController;

    private UUID authorId;
    private AuthorResponseDto authorDto;
    private AuthorRequestDto authorRequest;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        authorDto = AuthorResponseDto.builder()
                .id(authorId)
                .firstName("Robert")
                .lastName("Martin")
                .biography("Author of Clean Code")
                .nationality("USA")
                .build();

        authorRequest = AuthorRequestDto.builder()
                .firstName("Robert")
                .lastName("Martin")
                .biography("Author of Clean Code")
                .nationality("USA")
                .build();
    }

    @Test
    void getAllAuthors_ShouldReturnPage() {
        Page<AuthorResponseDto> page = new PageImpl<>(List.of(authorDto));
        when(authorService.getAllAuthors(any())).thenReturn(page);

        ResponseEntity<Page<AuthorResponseDto>> response = authorController.getAllAuthors(1, 20, "lastName", "asc", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getAllAuthors_WithSearch_ShouldCallSearch() {
        Page<AuthorResponseDto> page = new PageImpl<>(List.of(authorDto));
        when(authorService.searchAuthors(eq("Martin"), any())).thenReturn(page);

        ResponseEntity<Page<AuthorResponseDto>> response = authorController.getAllAuthors(1, 20, "lastName", "asc", "Martin");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authorService).searchAuthors(eq("Martin"), any());
        verify(authorService, never()).getAllAuthors(any());
    }

    @Test
    void getAuthorById_ShouldReturnAuthor() {
        when(authorService.getAuthorById(authorId)).thenReturn(authorDto);

        ResponseEntity<AuthorResponseDto> response = authorController.getAuthorById(authorId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Robert", response.getBody().getFirstName());
    }

    @Test
    void getAuthorBooks_ShouldReturnBooks() {
        BookResponseDto bookDto = BookResponseDto.builder()
                .id(UUID.randomUUID())
                .title("Clean Code")
                .build();
        Page<BookResponseDto> page = new PageImpl<>(List.of(bookDto));
        when(bookService.getBooksByAuthor(eq(authorId), any())).thenReturn(page);

        ResponseEntity<Page<BookResponseDto>> response = authorController.getAuthorBooks(authorId, 1, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void createAuthor_ShouldReturnCreated() {
        when(authorService.createAuthor(any(AuthorRequestDto.class))).thenReturn(authorDto);

        ResponseEntity<AuthorResponseDto> response = authorController.createAuthor(authorRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Robert", response.getBody().getFirstName());
    }

    @Test
    void updateAuthor_ShouldReturnOk() {
        when(authorService.updateAuthor(eq(authorId), any(AuthorRequestDto.class))).thenReturn(authorDto);

        ResponseEntity<AuthorResponseDto> response = authorController.updateAuthor(authorId, authorRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Martin", response.getBody().getLastName());
    }

    @Test
    void deleteAuthor_ShouldReturnNoContent() {
        doNothing().when(authorService).deleteAuthor(authorId);

        ResponseEntity<Void> response = authorController.deleteAuthor(authorId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(authorService).deleteAuthor(authorId);
    }

    @Test
    void getAllAuthors_PageIndex_ShouldBeZeroBased() {
        Page<AuthorResponseDto> page = new PageImpl<>(List.of());
        when(authorService.getAllAuthors(any())).thenReturn(page);

        authorController.getAllAuthors(1, 20, "lastName", "asc", null);

        verify(authorService).getAllAuthors(argThat(p -> p.getPageNumber() == 0));
    }
}
