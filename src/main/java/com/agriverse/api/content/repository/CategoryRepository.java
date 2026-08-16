package com.agriverse.api.content.repository;

import com.agriverse.api.content.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    Optional<Category> findByPublicId(UUID publicId);
    List<Category> findByParentCategoryIsNullOrderByDisplayOrderAsc();
    List<Category> findByParentCategoryIdOrderByDisplayOrderAsc(Long parentId);
    boolean existsBySlug(String slug);
    long countByParentCategoryId(Long parentId);
}
