package com.agriverse.api.reference.entity;

import com.agriverse.api.common.entity.BaseEntity;
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

/** Normalized master list of crops, avoiding repeated free-text crop names elsewhere. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "crops")
public class Crop extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
