package com.library.catalogue.service;

import com.library.catalogue.dto.BookResponseDto;
import com.library.catalogue.dto.BookRequestDto;
import com.library.catalogue.entity.BookEntity;
import com.library.catalogue.enums.BookStatus;
import com.library.catalogue.exception.BookNotFoundException;
import com.library.catalogue.exception.DuplicateIsbnException;
import com.library.catalogue.exception.InsufficientCopiesException;
import com.library.catalogue.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    @Cacheable(value = "books", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }

    @Cacheable(value = "book", key = "#id")
    public BookResponseDto getBookById(UUID id) {
        return bookRepository.findById(id)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public BookEntity getBookEntityById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    @CacheEvict(value = {"books", "book", "availableBooks"}, allEntries = true)
    public BookResponseDto createBook(BookRequestDto requestDto) {
        if (bookRepository.existsByIsbn(requestDto.getIsbn())) {
            throw new DuplicateIsbnException(requestDto.getIsbn());
        }

        BookEntity book = BookEntity.builder()
                .isbn(requestDto.getIsbn())
                .title(requestDto.getTitle())
                .subtitle(requestDto.getSubtitle())
                .description(requestDto.getDescription())
                .publicationDate(requestDto.getPublicationDate())
                .publisher(requestDto.getPublisher())
                .pageCount(requestDto.getPageCount())
                .language(requestDto.getLanguage())
                .format(requestDto.getFormat())
                .shelfLocation(requestDto.getShelfLocation())
                .totalCopies(requestDto.getTotalCopies())
                .availableCopies(requestDto.getAvailableCopies())
                .status(requestDto.getAvailableCopies() > 0 ? BookStatus.AVAILABLE : BookStatus.BORROWED)
                .averageRating(BigDecimal.ZERO)
                .build();

        BookEntity savedBook = bookRepository.save(book);
        log.info("Created new book: {} (ISBN: {})", savedBook.getTitle(), savedBook.getIsbn());

        return mapToResponseDto(savedBook);
    }

    @Transactional
    @CacheEvict(value = {"books", "book", "availableBooks"}, allEntries = true)
    public BookResponseDto updateBook(UUID id, BookRequestDto requestDto) {
        BookEntity existing = getBookEntityById(id);

        existing.setTitle(requestDto.getTitle());
        existing.setIsbn(requestDto.getIsbn());
        existing.setSubtitle(requestDto.getSubtitle());
        existing.setDescription(requestDto.getDescription());
        existing.setPublicationDate(requestDto.getPublicationDate());
        existing.setPublisher(requestDto.getPublisher());
        existing.setPageCount(requestDto.getPageCount());
        existing.setLanguage(requestDto.getLanguage());
        existing.setFormat(requestDto.getFormat());
        existing.setShelfLocation(requestDto.getShelfLocation());
        existing.setTotalCopies(requestDto.getTotalCopies());
        existing.setAvailableCopies(requestDto.getAvailableCopies());

        BookEntity updatedBook = bookRepository.save(existing);
        log.info("Updated book: {} (ID: {})", updatedBook.getTitle(), id);

        return mapToResponseDto(updatedBook);
    }

    @Transactional
    @CacheEvict(value = {"books", "book", "availableBooks"}, allEntries = true)
    public void deleteBook(UUID id) {
        BookEntity book = getBookEntityById(id);
        bookRepository.delete(book);
        log.info("Deleted book: {} (ID: {})", book.getTitle(), id);
    }

    @Cacheable(value = "availableBooks")
    public List<BookResponseDto> getAvailableBooks() {
        return bookRepository.findAvailableBooks()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional
    @CacheEvict(value = {"books", "book", "availableBooks"}, allEntries = true)
    public void borrowBook(UUID id) {
        BookEntity book = getBookEntityById(id);

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new InsufficientCopiesException(book.getTitle());
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        if (book.getAvailableCopies() == 0 && book.getStatus() != BookStatus.MAINTENANCE) {
            book.setStatus(BookStatus.BORROWED);
        }

        bookRepository.save(book);
        log.info("Book borrowed: {} (Available copies left: {})",
                book.getTitle(), book.getAvailableCopies());
    }

    @Transactional
    @CacheEvict(value = {"books", "book", "availableBooks"}, allEntries = true)
    public void returnBook(UUID id) {
        BookEntity book = getBookEntityById(id);

        if (book.getAvailableCopies() == null || book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new RuntimeException("All copies are already available: " + book.getTitle());
        }

        book.setAvailableCopies(book.getAvailableCopies() + 1);

        if (book.getStatus() == BookStatus.BORROWED && book.getAvailableCopies() > 0) {
            book.setStatus(BookStatus.AVAILABLE);
        }

        bookRepository.save(book);
        log.info("Book returned: {} (Available copies: {})",
                book.getTitle(), book.getAvailableCopies());
    }

    public boolean isBookAvailable(UUID id) {
        BookEntity book = getBookEntityById(id);
        return book.getAvailableCopies() != null && book.getAvailableCopies() > 0;
    }

    private BookResponseDto mapToResponseDto(BookEntity book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .subtitle(book.getSubtitle())
                .description(book.getDescription())
                .format(book.getFormat())
                .status(book.getStatus())
                .availableCopies(book.getAvailableCopies())
                .averageRating(book.getAverageRating())
                .build();
    }
}
