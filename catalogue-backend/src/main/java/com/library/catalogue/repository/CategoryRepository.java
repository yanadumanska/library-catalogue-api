package com.library.catalogue.repository;

import com.library.catalogue.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.subcategories WHERE c.parentCategory IS NULL")
    List<CategoryEntity> findRootCategoriesWithChildren();

    List<CategoryEntity> findByParentCategoryId(UUID parentId);
}
