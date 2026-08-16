package com.agriverse.api.ai.dto.internal;

import java.util.UUID;

/** One row of GET {ai-service}/internal/similar-articles?articleId=...&limit=... */
public record InternalSimilarArticle(UUID articleId, double score) {
}
