package com.agriverse.api.content.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full article detail for the author/editor edit form -- distinct from
 * {@link ArticleDetailResponse} (the public reader-facing view) because an
 * edit form needs to round-trip UUIDs (categoryId, tagIds) back into
 * UpdateArticleRequest, not just display names/slugs. Returned regardless
 * of publication status by GET /articles/{id}/edit, which is why it's
 * gated separately (AUTHOR-owns-it, or EDITOR/ADMIN) rather than public.
 */
public record ArticleEditResponse(
        UUID id,
        String title,
        String subtitle,
        String slug,
        String heroImageUrl,
        JsonNode body,
        String status,
        UUID categoryId,
        String categoryName,
        List<UUID> tagIds,
        List<String> tagNames,
        UUID authorId,
        String authorDisplayName,
        String seoMetaTitle,
        String seoMetaDescription,
        Instant publishedAt
) {
}
