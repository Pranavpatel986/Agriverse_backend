package com.agriverse.api.ai.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Published by ArticleService.updateStatus on the DRAFT/IN_REVIEW ->
 * PUBLISHED transition. Deliberately a plain event rather than a direct
 * method call into the ai package, so content/ stays unaware that AI
 * indexing exists at all — matching the "AI functionality is deliberately
 * isolated" principle in the Software Architecture Document §7.
 */
public record ArticlePublishedEvent(UUID articleId, String title, String slug, JsonNode body) {
}
