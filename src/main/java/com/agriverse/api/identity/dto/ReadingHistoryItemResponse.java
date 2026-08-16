package com.agriverse.api.identity.dto;

import java.time.Instant;
import java.util.UUID;

public record ReadingHistoryItemResponse(UUID articleId, String title, String slug, Instant readAt) {
}
