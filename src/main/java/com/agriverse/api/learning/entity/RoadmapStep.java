package com.agriverse.api.learning.entity;

import com.agriverse.api.common.entity.BaseEntity;
import com.agriverse.api.content.entity.Article;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One ordered step within a Roadmap, optionally linking to an Article.
 *
 * NOTE: extends {@link BaseEntity} (adding a {@code publicId}) rather than
 * being a bare-id table as the Database Design Specification's table lists
 * it -- the REST API Specification's Roadmap detail/progress endpoints
 * (Section 9) require an opaque, non-enumerable step identifier to expose
 * in {@code steps[].id} and accept back in
 * {@code PATCH /roadmaps/{id}/progress}. This is a resolved gap between
 * the two specs, consistent with the DB spec's own external-ID convention
 * for every other entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roadmap_steps", uniqueConstraints = @UniqueConstraint(columnNames = {"roadmap_id", "step_order"}))
public class RoadmapStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(nullable = false, length = 255)
    private String title;
}
