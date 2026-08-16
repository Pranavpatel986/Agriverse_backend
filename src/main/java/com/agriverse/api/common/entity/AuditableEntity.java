package com.agriverse.api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Adds a nullable {@code updatedAt} audit column, per the DB spec's
 * convention: present on every mutable table, omitted on append-only
 * log/junction tables (which extend {@link BaseEntity} directly instead).
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
