package com.agriverse.api.engagement.repository;

import com.agriverse.api.engagement.entity.Comment;
import com.agriverse.api.engagement.entity.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByArticleIdAndStatusOrderByCreatedAtDesc(Long articleId, ModerationStatus status, Pageable pageable);
    Optional<Comment> findByPublicIdAndDeletedAtIsNull(UUID publicId);

    @Query("select c from Comment c where c.status = com.agriverse.api.engagement.entity.ModerationStatus.FLAGGED and c.deletedAt is null")
    Page<Comment> findModerationQueue(Pageable pageable);
}
