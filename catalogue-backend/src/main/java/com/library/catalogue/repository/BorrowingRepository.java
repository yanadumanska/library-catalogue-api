package com.library.catalogue.repository;

import com.library.catalogue.entity.BorrowingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BorrowingRepository extends JpaRepository<BorrowingEntity, UUID> {

    List<BorrowingEntity> findByUserId(UUID userId);

    List<BorrowingEntity> findByUserIdAndStatus(UUID userId, String status);

    List<BorrowingEntity> findByBookId(UUID bookId);

    Optional<BorrowingEntity> findByBookIdAndUserIdAndStatus(UUID bookId, UUID userId, String status);

    long countByUserIdAndStatus(UUID userId, String status);
}
