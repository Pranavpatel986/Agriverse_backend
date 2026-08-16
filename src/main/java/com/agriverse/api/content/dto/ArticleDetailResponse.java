package com.agriverse.api.content.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record ArticleDetailResponse(
        UUID id,
        String title,
        JsonNode body,
        List<String> tags,
        CategoryRef category,
        AuthorDetailRef author,
        SeoRef seo
) {
}
