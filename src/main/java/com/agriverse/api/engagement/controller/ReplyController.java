package com.agriverse.api.engagement.controller;

import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.engagement.dto.LikeCountResponse;
import com.agriverse.api.engagement.service.ReplyLikeService;
import com.agriverse.api.engagement.service.CommentService;
import com.agriverse.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Reply endpoints — REST API Specification, Section 7. Liking mirrors the
 * comment-like endpoints ("the equivalent /replies/{id}/like endpoint").
 */
@RestController
@RequestMapping("/api/v1/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final CommentService commentService;
    private final ReplyLikeService replyLikeService;

    @DeleteMapping("/{id}")
    public MessageResponse delete(@PathVariable UUID id) {
        commentService.deleteReply(SecurityUtils.currentUserId(), id);
        return new MessageResponse("Reply deleted");
    }

    @PostMapping("/{id}/like")
    public LikeCountResponse like(@PathVariable UUID id) {
        return replyLikeService.like(SecurityUtils.currentUserId(), id);
    }

    @DeleteMapping("/{id}/like")
    public LikeCountResponse unlike(@PathVariable UUID id) {
        return replyLikeService.unlike(SecurityUtils.currentUserId(), id);
    }
}
