package com.agriverse.api.recommendation.controller;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.content.dto.ArticleSummaryResponse;
import com.agriverse.api.recommendation.dto.RecommendedArticleResponse;
import com.agriverse.api.recommendation.service.RecommendationService;
import com.agriverse.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Recommendations endpoint group — REST API Specification, Section 11. */
@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/api/v1/recommendations")
    public ContentListResponse<RecommendedArticleResponse> forCurrentUser(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return recommendationService.forUser(SecurityUtils.currentUserId(), Math.min(limit, 25));
    }

    @GetMapping("/api/v1/articles/{id}/recommendations")
    public ContentListResponse<ArticleSummaryResponse> forArticle(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return recommendationService.forArticle(id, Math.min(limit, 10));
    }
}
