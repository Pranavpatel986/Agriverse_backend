package com.agriverse.api.engagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookmarkRequest(@NotBlank String entityType, @NotNull UUID entityId) {
}
