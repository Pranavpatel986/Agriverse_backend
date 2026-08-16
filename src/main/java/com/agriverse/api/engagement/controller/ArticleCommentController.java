package com.agriverse.api.engagement.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.engagement.dto.CommentCreatedResponse;
import com.agriverse.api.engagement.dto.CommentResponse;
import com.agriverse.api.engagement.dto.CreateCommentRequest;
import com.agriverse.api.engagement.service.CommentService;
import com.agriverse.api.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Article-nested comment endpoints — REST API Specification, Section 7. */
@RestController
@RequestMapping("/api/v1/articles/{articleId}/comments")
@RequiredArgsConstructor
public class ArticleCommentController {

    private final CommentService commentService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    public ContentListResponse<CommentResponse> list(@PathVariable UUID articleId,
                                                       @RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, paginationProperties.getDefaultPageSize(), paginationProperties.getMaxPageSize());
        return commentService.listForArticle(articleId, pageable);
    }

    @PostMapping
    public ResponseEntity<CommentCreatedResponse> create(@PathVariable UUID articleId, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(SecurityUtils.currentUserId(), articleId, request));
    }
}
