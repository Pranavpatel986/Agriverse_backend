package com.agriverse.api.admin.controller;

import com.agriverse.api.admin.dto.*;
import com.agriverse.api.admin.service.AdminArticleService;
import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Admin > Articles — REST API Specification, Section 12. EDITOR/ADMIN (enforced by SecurityConfig's admin gate). */
@RestController
@RequestMapping("/api/v1/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final AdminArticleService adminArticleService;

    @GetMapping
    public PageResponse<AdminArticleSummaryResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 20, 100);
        return adminArticleService.list(status, authorId, pageable);
    }

    @PatchMapping("/{id}/review")
    public ArticleReviewResponse review(@PathVariable UUID id, @Valid @RequestBody ArticleReviewRequest request) {
        return adminArticleService.review(id, request);
    }
}
