package com.library.catalogue.repository;

import com.library.catalogue.entity.BookEntity;
import com.library.catalogue.enums.BookFormat;
import com.library.catalogue.enums.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID> {

    Optional<BookEntity> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<BookEntity> findByStatus(BookStatus status);

    @Query("SELECT b FROM BookEntity b WHERE b.availableCopies > 0")
    List<BookEntity> findAvailableBooks();

    @Query("SELECT b FROM BookEntity b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:isbn IS NULL OR b.isbn = :isbn) AND " +
            "(:format IS NULL OR b.format = :format) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:minRating IS NULL OR b.averageRating >= :minRating)")
    Page<BookEntity> findAllWithFilters(
            @Param("title") String title,
            @Param("isbn") String isbn,
            @Param("format") BookFormat format,
            @Param("status") BookStatus status,
            @Param("minRating") BigDecimal minRating,
            Pageable pageable
    );

    Page<BookEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<BookEntity> findTop10ByOrderByAverageRatingDesc();

    @Query("UPDATE BookEntity b SET b.availableCopies = b.availableCopies - 1 WHERE b.id = :bookId AND b.availableCopies > 0")
    int decrementAvailableCopies(@Param("bookId") UUID bookId);

    @Query("UPDATE BookEntity b SET b.availableCopies = b.availableCopies + 1 WHERE b.id = :bookId AND b.availableCopies < b.totalCopies")
    int incrementAvailableCopies(@Param("bookId") UUID bookId);
}
