package com.library.catalogue.controller;

import com.library.catalogue.dto.AuthorResponseDto;
import com.library.catalogue.dto.BookResponseDto;
import com.library.catalogue.dto.BookRequestDto;
import com.library.catalogue.dto.CategoryResponseDto;
import com.library.catalogue.enums.BookFormat;
import com.library.catalogue.enums.BookStatus;
import com.library.catalogue.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<Page<BookResponseDto>> getAllBooks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "title:asc") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BookFormat format,
            @RequestParam(required = false) BookStatus status,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) LocalDate publishedAfter,
            @RequestParam(required = false) LocalDate publishedBefore) {

        int pageIndex = Math.max(page - 1, 0);
        Sort sorting = parseSort(sort);
        Pageable pageable = PageRequest.of(pageIndex, limit, sorting);

        return ResponseEntity.ok()
                .body(bookService.getBooksWithFilters(
                        search, format, status, language, minRating,
                        publishedAfter, publishedBefore, author, category, pageable));
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(":");
        String field = parts[0];
        String direction = parts.length > 1 ? parts[1] : "asc";
        return direction.equalsIgnoreCase("desc")
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(bookService.getBookById(id));
    }

    @GetMapping("/{id}/authors")
    public ResponseEntity<List<AuthorResponseDto>> getBookAuthors(@PathVariable UUID id) {
        BookResponseDto book = bookService.getBookById(id);
        return ResponseEntity.ok(book.getAuthors());
    }

    @GetMapping("/{id}/categories")
    public ResponseEntity<List<CategoryResponseDto>> getBookCategories(@PathVariable UUID id) {
        BookResponseDto book = bookService.getBookById(id);
        return ResponseEntity.ok(book.getCategories());
    }

    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookRequestDto requestDto) {
        BookResponseDto created = bookService.createBook(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody BookRequestDto requestDto) {
        return ResponseEntity.ok(bookService.updateBook(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/available")
    public ResponseEntity<Boolean> isBookAvailable(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.isBookAvailable(id));
    }
}
