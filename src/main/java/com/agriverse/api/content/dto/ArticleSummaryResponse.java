package com.agriverse.api.content.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleSummaryResponse(
        UUID id,
        String title,
        String slug,
        String excerpt,
        String heroImageUrl,
        CategoryRef category,
        AuthorRef author,
        Integer readingTimeMinutes,
        Instant publishedAt
) {
}
