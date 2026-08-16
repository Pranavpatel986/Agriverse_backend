package com.agriverse.api.learning.entity;

import com.agriverse.api.common.entity.AuditableEntity;
import com.agriverse.api.content.entity.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A structured, ordered learning path composed of sequential steps. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roadmaps")
public class Roadmap extends AuditableEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 280)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
