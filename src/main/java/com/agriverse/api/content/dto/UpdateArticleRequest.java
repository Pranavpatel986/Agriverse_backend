package com.agriverse.api.content.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UpdateArticleRequest(
        @Size(min = 5, max = 255) String title,
        @Size(max = 400) String subtitle,
        UUID categoryId,
        Set<UUID> tagIds,
        JsonNode body,
        String heroImageUrl
) {
    public boolean isEmpty() {
        return title == null && subtitle == null && categoryId == null && tagIds == null && body == null && heroImageUrl == null;
    }
}
