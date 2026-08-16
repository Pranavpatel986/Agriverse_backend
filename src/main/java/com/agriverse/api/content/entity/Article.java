package com.agriverse.api.content.entity;

import com.agriverse.api.common.entity.SoftDeletableEntity;
import com.agriverse.api.identity.entity.Author;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** The core content entity of AgriVerse (crop guide, disease profile, machinery overview, etc.). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "articles")
public class Article extends SoftDeletableEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(length = 400)
    private String subtitle;

    @Column(name = "hero_image_url", length = 500)
    private String heroImageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode body;

    @Column(length = 500)
    private String excerpt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "reading_time_minutes")
    private Integer readingTimeMinutes;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "seo_meta_title", length = 255)
    private String seoMetaTitle;

    @Column(name = "seo_meta_description", length = 500)
    private String seoMetaDescription;
}
