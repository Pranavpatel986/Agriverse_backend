package com.agriverse.api.engagement.dto;

import java.time.Instant;
import java.util.UUID;

public record BookmarkResponse(UUID id, String entityType, UUID entityId, BookmarkEntitySummary entitySummary, Instant createdAt) {
}
