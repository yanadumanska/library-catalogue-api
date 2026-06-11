package com.library.catalogue.repository;

import com.library.catalogue.entity.AuthorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID> {

    List<AuthorEntity> findByNationality(String nationality);

    @Query("SELECT a FROM AuthorEntity a WHERE " +
            "a.firstName ILIKE CONCAT('%', :search, '%') OR " +
            "a.lastName ILIKE CONCAT('%', :search, '%')")
    Page<AuthorEntity> searchByName(@Param("search") String search, Pageable pageable);
}
