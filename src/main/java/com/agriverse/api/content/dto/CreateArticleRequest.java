package com.agriverse.api.content.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateArticleRequest(
        @NotBlank @Size(min = 5, max = 255) String title,
        @Size(max = 400) String subtitle,
        @NotNull UUID categoryId,
        Set<UUID> tagIds,
        @NotNull JsonNode body,
        String heroImageUrl
) {
}
