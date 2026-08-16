package com.agriverse.api.content.repository;

import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.entity.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Article> findByPublicIdAndDeletedAtIsNull(UUID publicId);

    Optional<Article> findByPublicId(UUID publicId);

    boolean existsBySlug(String slug);

    @Query("select a from Article a where a.status = com.agriverse.api.content.entity.ArticleStatus.PUBLISHED " +
            "and a.deletedAt is null " +
            "and (:categorySlug is null or a.category.slug = :categorySlug) " +
            "and (:authorPublicId is null or a.author.user.publicId = :authorPublicId) " +
            "and (:tagId is null or exists (select 1 from ArticleTag atg where atg.article = a and atg.tag.id = :tagId))")
    Page<Article> browsePublished(@Param("categorySlug") String categorySlug,
                                   @Param("authorPublicId") UUID authorPublicId,
                                   @Param("tagId") Long tagId,
                                   Pageable pageable);

    @Query("select a from Article a where a.status = com.agriverse.api.content.entity.ArticleStatus.PUBLISHED " +
            "and a.deletedAt is null and a.category = :category and a.id <> :excludeId")
    List<Article> findRelatedByCategory(@Param("category") com.agriverse.api.content.entity.Category category,
                                         @Param("excludeId") Long excludeId,
                                         Pageable pageable);

    @Query("select a from Article a where a.deletedAt is null " +
            "and (:status is null or a.status = :status) " +
            "and (:authorPublicId is null or a.author.user.publicId = :authorPublicId)")
    Page<Article> adminSearch(@Param("status") ArticleStatus status, @Param("authorPublicId") UUID authorPublicId, Pageable pageable);

    @Modifying
    @Query("update Article a set a.viewCount = a.viewCount + 1 where a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("select a from Article a where a.deletedAt is null and lower(a.title) like lower(concat('%', :q, '%'))")
    Page<Article> searchByTitle(@Param("q") String q, Pageable pageable);

    long countByAuthorIdAndStatus(Long authorId, ArticleStatus status);
}
