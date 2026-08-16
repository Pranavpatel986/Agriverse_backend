package com.agriverse.api.identity.entity;

import com.agriverse.api.common.entity.AuditableEntity;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Optional 1:1 extension of User with public-facing author profile data. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "authors")
public class Author extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 255)
    private String credentials;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_links", columnDefinition = "jsonb")
    private JsonNode socialLinks;

    @Column(name = "articles_published_count", nullable = false)
    private Integer articlesPublishedCount = 0;
}
