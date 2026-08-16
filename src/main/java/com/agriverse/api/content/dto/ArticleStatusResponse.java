package com.agriverse.api.content.dto;

import java.time.Instant;
import java.util.UUID;

public record ArticleStatusResponse(UUID id, String status, Instant publishedAt) {
}
