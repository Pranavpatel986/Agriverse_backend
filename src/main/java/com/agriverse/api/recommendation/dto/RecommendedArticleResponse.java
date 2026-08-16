package com.agriverse.api.recommendation.dto;

import java.util.UUID;

public record RecommendedArticleResponse(UUID id, String title, String slug, String reason) {
}
