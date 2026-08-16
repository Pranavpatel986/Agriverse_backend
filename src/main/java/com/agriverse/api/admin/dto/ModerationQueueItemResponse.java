package com.agriverse.api.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record ModerationQueueItemResponse(UUID id, String entityType, String body, AdminAuthorRef user, String flagReason, Instant createdAt) {
}
