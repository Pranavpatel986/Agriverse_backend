package com.agriverse.api.content.entity;

import com.agriverse.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Freeform, cross-cutting label applied to Articles, independent of the Category hierarchy. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;
}
