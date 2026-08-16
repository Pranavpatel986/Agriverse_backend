package com.agriverse.api.admin.service;

import com.agriverse.api.admin.dto.*;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.engagement.entity.Comment;
import com.agriverse.api.engagement.entity.ModerationStatus;
import com.agriverse.api.engagement.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Backs the Admin > Comments moderation queue (REST API Specification,
 * Section 12). Scoped to Comments per the DB spec's moderation model;
 * Replies use the same {@link ModerationStatus} enum and can be extended
 * here identically if reply-level flagging is enabled.
 */
@Service
@RequiredArgsConstructor
public class AdminModerationService {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public ContentListResponse<ModerationQueueItemResponse> queue(Pageable pageable) {
        var page = commentRepository.findModerationQueue(pageable);
        return new ContentListResponse<>(page.getContent().stream()
                .map(c -> new ModerationQueueItemResponse(c.getPublicId(), "comment", c.getBody(),
                        new AdminAuthorRef(c.getUser().getFullName()), "auto: possible spam", c.getCreatedAt()))
                .toList());
    }

    @Transactional
    public ModerateCommentResponse moderate(UUID id, ModerateCommentRequest request) {
        Comment comment = commentRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", id));
        String action = request.action().trim().toLowerCase();
        if (action.equals("approve")) {
            comment.setStatus(ModerationStatus.VISIBLE);
        } else if (action.equals("remove")) {
            comment.setStatus(ModerationStatus.DELETED);
            comment.setDeletedAt(Instant.now());
        } else {
            throw new BadRequestException("action must be 'approve' or 'remove'");
        }
        commentRepository.save(comment);
        return new ModerateCommentResponse(comment.getPublicId(), comment.getStatus().name().toLowerCase());
    }
}
