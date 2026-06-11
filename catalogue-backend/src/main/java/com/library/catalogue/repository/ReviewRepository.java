package com.library.catalogue.repository;

import com.library.catalogue.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findByBookId(UUID bookId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.book.id = :bookId")
    BigDecimal calculateAverageRating(@Param("bookId") UUID bookId);

    boolean existsByBookIdAndUserId(UUID bookId, UUID userId);
}
