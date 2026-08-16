package com.agriverse.api.engagement.dto;

import java.time.Instant;
import java.util.UUID;

public record ReplyResponse(UUID id, String body, UserRef user, int likeCount, Instant createdAt) {
}
