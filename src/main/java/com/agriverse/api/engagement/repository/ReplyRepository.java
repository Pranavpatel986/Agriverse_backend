package com.agriverse.api.engagement.repository;

import com.agriverse.api.engagement.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByCommentIdAndStatusOrderByCreatedAtAsc(Long commentId, com.agriverse.api.engagement.entity.ModerationStatus status);
    Optional<Reply> findByPublicIdAndDeletedAtIsNull(UUID publicId);
}
