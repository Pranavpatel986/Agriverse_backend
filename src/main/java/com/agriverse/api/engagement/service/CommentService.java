package com.agriverse.api.engagement.service;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.exception.ForbiddenException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.engagement.dto.*;
import com.agriverse.api.engagement.entity.*;
import com.agriverse.api.engagement.repository.CommentRepository;
import com.agriverse.api.engagement.repository.LikeRepository;
import com.agriverse.api.engagement.repository.ReplyRepository;
import com.agriverse.api.identity.entity.User;
import com.agriverse.api.identity.repository.UserRepository;
import com.agriverse.api.security.SecurityUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Backs the Comments endpoint group (REST API Specification, Section 7) — comments, replies, and likes. */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final LikeRepository likeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ContentListResponse<CommentResponse> listForArticle(UUID articleId, Pageable pageable) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(articleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", articleId));
        var page = commentRepository.findByArticleIdAndStatusOrderByCreatedAtDesc(article.getId(), ModerationStatus.VISIBLE, pageable);
        return new ContentListResponse<>(page.getContent().stream().map(this::toResponse).toList());
    }

    @Transactional
    public CommentCreatedResponse create(Long userId, UUID articleId, CreateCommentRequest request) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(articleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", articleId));
        User user = userRepository.getReferenceById(userId);

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setBody(request.body().trim());
        comment.setStatus(containsSuspiciousContent(request.body()) ? ModerationStatus.FLAGGED : ModerationStatus.VISIBLE);
        commentRepository.save(comment);
        return new CommentCreatedResponse(comment.getPublicId(), comment.getStatus().name().toLowerCase());
    }

    @Transactional
    public CommentUpdatedResponse update(Long userId, UUID id, UpdateCommentRequest request) {
        Comment comment = commentRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", id));
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Only the comment's author may edit it");
        }
        comment.setBody(request.body().trim());
        commentRepository.save(comment);
        return new CommentUpdatedResponse(comment.getPublicId(), comment.getBody());
    }

    @Transactional
    public void delete(Long userId, UUID id) {
        Comment comment = commentRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", id));
        assertOwnerOrModerator(comment.getUser().getId(), userId);
        comment.setDeletedAt(Instant.now());
        comment.setStatus(ModerationStatus.DELETED);
        commentRepository.save(comment);
    }

    @Transactional
    public ReplyCreatedResponse createReply(Long userId, UUID commentId, CreateCommentRequest request) {
        Comment comment = commentRepository.findByPublicIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));
        User user = userRepository.getReferenceById(userId);

        Reply reply = new Reply();
        reply.setComment(comment);
        reply.setUser(user);
        reply.setBody(request.body().trim());
        reply.setStatus(ModerationStatus.VISIBLE);
        replyRepository.save(reply);

        // Notify the comment's author — but not when someone replies to their
        // own comment, which would just be noise.
        User commentAuthor = comment.getUser();
        if (commentAuthor != null && !commentAuthor.getId().equals(userId)) {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("articleId", comment.getArticle().getPublicId().toString());
            payload.put("articleSlug", comment.getArticle().getSlug());
            payload.put("commentId", comment.getPublicId().toString());
            payload.put("replierName", user.getFullName());
            notificationService.create(commentAuthor, NotificationType.COMMENT_REPLY, payload);
        }

        return new ReplyCreatedResponse(reply.getPublicId());
    }

    @Transactional
    public void deleteReply(Long userId, UUID id) {
        Reply reply = replyRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Reply", id));
        assertOwnerOrModerator(reply.getUser().getId(), userId);
        reply.setDeletedAt(Instant.now());
        reply.setStatus(ModerationStatus.DELETED);
        replyRepository.save(reply);
    }

    @Transactional
    public LikeCountResponse likeComment(Long userId, UUID commentId) {
        Comment comment = commentRepository.findByPublicIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));
        if (likeRepository.findByUserIdAndEntityTypeAndEntityId(userId, LikeableType.COMMENT, comment.getId()).isEmpty()) {
            Like like = new Like();
            like.setUser(userRepository.getReferenceById(userId));
            like.setEntityType(LikeableType.COMMENT);
            like.setEntityId(comment.getId());
            likeRepository.save(like);
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentRepository.save(comment);
        }
        return new LikeCountResponse(comment.getLikeCount());
    }

    @Transactional
    public LikeCountResponse unlikeComment(Long userId, UUID commentId) {
        Comment comment = commentRepository.findByPublicIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Comment", commentId));
        likeRepository.findByUserIdAndEntityTypeAndEntityId(userId, LikeableType.COMMENT, comment.getId())
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                    commentRepository.save(comment);
                });
        return new LikeCountResponse(comment.getLikeCount());
    }

    private void assertOwnerOrModerator(Long ownerId, Long currentUserId) {
        if (!ownerId.equals(currentUserId) && !SecurityUtils.hasAnyRole("EDITOR", "ADMIN")) {
            throw new ForbiddenException("Only the author or a moderator may perform this action");
        }
    }

    private boolean containsSuspiciousContent(String body) {
        // Lightweight placeholder for a spam/profanity classifier per the API spec's validation rule;
        // swap for a real moderation service (e.g. a hosted content-moderation API) without changing callers.
        String lower = body.toLowerCase();
        return lower.contains("http://") || lower.contains("https://") || lower.contains("buy now");
    }

    private CommentResponse toResponse(Comment comment) {
        long replyCount = replyRepository.findByCommentIdAndStatusOrderByCreatedAtAsc(comment.getId(), ModerationStatus.VISIBLE).size();
        return new CommentResponse(comment.getPublicId(), comment.getBody(), new UserRef(comment.getUser().getFullName()),
                comment.getLikeCount(), replyCount, comment.getCreatedAt());
    }
}
