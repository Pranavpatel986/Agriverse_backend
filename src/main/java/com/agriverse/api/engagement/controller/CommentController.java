package com.agriverse.api.engagement.controller;

import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.engagement.dto.*;
import com.agriverse.api.engagement.service.CommentService;
import com.agriverse.api.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Top-level comment endpoints (edit/delete/reply/like) — REST API Specification, Section 7. */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PutMapping("/{id}")
    public CommentUpdatedResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        commentService.delete(SecurityUtils.currentUserId(), id);
        return new MessageResponse("Comment deleted");
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<ReplyCreatedResponse> reply(@PathVariable UUID id, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createReply(SecurityUtils.currentUserId(), id, request));
    }

    @PostMapping("/{id}/like")
    public LikeCountResponse like(@PathVariable UUID id) {
        return commentService.likeComment(SecurityUtils.currentUserId(), id);
    }

    @DeleteMapping("/{id}/like")
    public LikeCountResponse unlike(@PathVariable UUID id) {
        return commentService.unlikeComment(SecurityUtils.currentUserId(), id);
    }
}
