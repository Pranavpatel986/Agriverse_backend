package com.agriverse.api.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Adds a nullable {@code deletedAt} marker for entities that use soft
 * deletes (Users, Articles, Comments, Replies) per the DB spec's
 * moderation/account-recovery rationale, instead of a hard DELETE.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
