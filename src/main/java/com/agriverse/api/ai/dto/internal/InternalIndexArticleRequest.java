package com.agriverse.api.ai.dto.internal;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Sent to POST {ai-service}/internal/index on publish. body is the same
 * rich-text JsonNode the API already stores (see ArticleDetailResponse) —
 * the AI service, not the Java layer, is responsible for flattening it to
 * plain text before chunking/embedding, since that logic will change as
 * the editor's rich-text schema evolves and is easier to iterate on in
 * Python than to keep in sync across two languages.
 */
public record InternalIndexArticleRequest(UUID articleId, String title, String slug, JsonNode body) {
}
