package com.agriverse.api.engagement.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(UUID id, String body, UserRef user, int likeCount, long replyCount, Instant createdAt) {
}
