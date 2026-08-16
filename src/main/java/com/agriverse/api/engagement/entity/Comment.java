package com.agriverse.api.engagement.entity;

import com.agriverse.api.common.entity.SoftDeletableEntity;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.identity.entity.User;
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

/** A top-level discussion post on an Article. Replies are a separate entity, capping nesting at one level. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationStatus status = ModerationStatus.VISIBLE;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;
}
