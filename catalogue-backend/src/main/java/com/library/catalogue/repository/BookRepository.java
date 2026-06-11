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
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID> {

    Optional<BookEntity> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM BookEntity b WHERE " +
            "(CAST(:search AS string) IS NULL OR b.title ILIKE CONCAT('%', CAST(:search AS string), '%') " +
            "OR b.isbn ILIKE CONCAT('%', CAST(:search AS string), '%') " +
            "OR b.description ILIKE CONCAT('%', CAST(:search AS string), '%')) AND " +
            "(:format IS NULL OR b.format = :format) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:language IS NULL OR b.language = :language) AND " +
            "(:minRating IS NULL OR b.averageRating >= :minRating) AND " +
            "(:publishedAfter IS NULL OR b.publicationDate >= :publishedAfter) AND " +
            "(:publishedBefore IS NULL OR b.publicationDate <= :publishedBefore) AND " +
            "(CAST(:authorName AS string) IS NULL OR EXISTS (SELECT 1 FROM b.authors a WHERE CONCAT(a.firstName, ' ', a.lastName) ILIKE CONCAT('%', CAST(:authorName AS string), '%'))) AND " +
            "(CAST(:categoryName AS string) IS NULL OR EXISTS (SELECT 1 FROM b.categories c WHERE c.name ILIKE CONCAT('%', CAST(:categoryName AS string), '%')))")
    Page<BookEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("format") BookFormat format,
            @Param("status") BookStatus status,
            @Param("language") String language,
            @Param("minRating") BigDecimal minRating,
            @Param("publishedAfter") LocalDate publishedAfter,
            @Param("publishedBefore") LocalDate publishedBefore,
            @Param("authorName") String authorName,
            @Param("categoryName") String categoryName,
            Pageable pageable);

    @Query("SELECT b FROM BookEntity b JOIN b.authors a WHERE a.id = :authorId")
    Page<BookEntity> findBooksByAuthorId(@Param("authorId") UUID authorId, Pageable pageable);

    @Query("SELECT b FROM BookEntity b JOIN b.categories c WHERE c.id = :categoryId")
    Page<BookEntity> findBooksByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);
}
