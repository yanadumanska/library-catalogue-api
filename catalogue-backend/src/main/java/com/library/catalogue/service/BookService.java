package com.library.catalogue.service;

import com.library.catalogue.dto.AuthorResponseDto;
import com.library.catalogue.dto.BookResponseDto;
import com.library.catalogue.dto.BookRequestDto;
import com.library.catalogue.dto.CategoryResponseDto;
import com.library.catalogue.entity.BookEntity;
import com.library.catalogue.enums.BookFormat;
import com.library.catalogue.enums.BookStatus;
import com.library.catalogue.exception.BookNotFoundException;
import com.library.catalogue.exception.DuplicateIsbnException;
import com.library.catalogue.repository.AuthorRepository;
import com.library.catalogue.repository.BookRepository;
import com.library.catalogue.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    @Cacheable(value = "books", key = "'filter-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #search + '-' + #format + '-' + #status + '-' + #language + '-' + #minRating + '-' + #authorName + '-' + #categoryName")
    public Page<BookResponseDto> getBooksWithFilters(
            String search, BookFormat format, BookStatus status, String language,
            BigDecimal minRating, LocalDate publishedAfter, LocalDate publishedBefore,
            String authorName, String categoryName, Pageable pageable) {
        return bookRepository.findAllWithFilters(
                        search, format, status, language, minRating,
                        publishedAfter, publishedBefore, authorName, categoryName, pageable)
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

    @Cacheable(value = "books", key = "'author-' + #authorId + '-' + #pageable.pageNumber")
    public Page<BookResponseDto> getBooksByAuthor(UUID authorId, Pageable pageable) {
        return bookRepository.findBooksByAuthorId(authorId, pageable)
                .map(this::mapToResponseDto);
    }

    @Cacheable(value = "books", key = "'category-' + #categoryId + '-' + #pageable.pageNumber")
    public Page<BookResponseDto> getBooksByCategory(UUID categoryId, Pageable pageable) {
        return bookRepository.findBooksByCategoryId(categoryId, pageable)
                .map(this::mapToResponseDto);
    }

    @Transactional
    @CacheEvict(value = {"books", "book"}, allEntries = true)
    public BookResponseDto createBook(BookRequestDto requestDto) {
        if (bookRepository.existsByIsbn(requestDto.getIsbn())) {
            throw new DuplicateIsbnException(requestDto.getIsbn());
        }

        BookEntity book = buildBookEntity(requestDto);
        setBookRelations(book, requestDto.getAuthorIds(), requestDto.getCategoryIds());

        BookEntity saved = bookRepository.save(book);
        log.info("Created book: {} (ISBN: {})", saved.getTitle(), saved.getIsbn());
        return mapToResponseDto(saved);
    }

    @Transactional
    @CacheEvict(value = {"books", "book"}, allEntries = true)
    public BookResponseDto updateBook(UUID id, BookRequestDto requestDto) {
        BookEntity existing = getBookEntityById(id);
        updateBookFields(existing, requestDto);
        setBookRelations(existing, requestDto.getAuthorIds(), requestDto.getCategoryIds());

        BookEntity updated = bookRepository.save(existing);
        log.info("Updated book: {} (ID: {})", updated.getTitle(), id);
        return mapToResponseDto(updated);
    }

    @Transactional
    @CacheEvict(value = {"books", "book"}, allEntries = true)
    public void deleteBook(UUID id) {
        BookEntity book = getBookEntityById(id);
        bookRepository.delete(book);
        log.info("Deleted book: {} (ID: {})", book.getTitle(), id);
    }

    public boolean isBookAvailable(UUID id) {
        BookEntity book = getBookEntityById(id);
        return book.getAvailableCopies() != null && book.getAvailableCopies() > 0;
    }

    private BookEntity buildBookEntity(BookRequestDto dto) {
        return BookEntity.builder()
                .isbn(dto.getIsbn())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .publicationDate(dto.getPublicationDate())
                .publisher(dto.getPublisher())
                .pageCount(dto.getPageCount())
                .language(dto.getLanguage())
                .format(dto.getFormat())
                .shelfLocation(dto.getShelfLocation())
                .totalCopies(dto.getTotalCopies())
                .availableCopies(dto.getAvailableCopies())
                .status(dto.getAvailableCopies() != null && dto.getAvailableCopies() > 0
                        ? BookStatus.AVAILABLE : BookStatus.BORROWED)
                .averageRating(BigDecimal.ZERO)
                .build();
    }

    private void updateBookFields(BookEntity book, BookRequestDto dto) {
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setSubtitle(dto.getSubtitle());
        book.setDescription(dto.getDescription());
        book.setPublicationDate(dto.getPublicationDate());
        book.setPublisher(dto.getPublisher());
        book.setPageCount(dto.getPageCount());
        book.setLanguage(dto.getLanguage());
        book.setFormat(dto.getFormat());
        book.setShelfLocation(dto.getShelfLocation());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getAvailableCopies());
    }

    private void setBookRelations(BookEntity book, List<UUID> authorIds, List<UUID> categoryIds) {
        if (authorIds != null) {
            book.setAuthors(authorRepository.findAllById(authorIds));
        }
        if (categoryIds != null) {
            book.setCategories(categoryRepository.findAllById(categoryIds));
        }
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
                .authors(book.getAuthors() != null ? book.getAuthors().stream()
                        .map(a -> AuthorResponseDto.builder()
                                .id(a.getId())
                                .firstName(a.getFirstName())
                                .lastName(a.getLastName())
                                .build())
                        .toList() : null)
                .categories(book.getCategories() != null ? book.getCategories().stream()
                        .map(c -> CategoryResponseDto.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .build())
                        .toList() : null)
                .build();
    }
}
