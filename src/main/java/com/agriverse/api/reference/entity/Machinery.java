package com.agriverse.api.reference.entity;

import com.agriverse.api.common.entity.AuditableEntity;
import com.agriverse.api.content.entity.Article;
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

import java.math.BigDecimal;

/** A reference entry for farm machinery/equipment, including indicative pricing. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "machinery")
public class Machinery extends AuditableEntity {

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MachineryCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_range_min", precision = 12, scale = 2)
    private BigDecimal priceRangeMin;

    @Column(name = "price_range_max", precision = 12, scale = 2)
    private BigDecimal priceRangeMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicable_crop_id")
    private Crop applicableCrop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;
}
