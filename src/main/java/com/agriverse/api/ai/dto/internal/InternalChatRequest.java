package com.agriverse.api.ai.dto.internal;

import java.util.UUID;

/**
 * Wire shape sent to POST {ai-service}/internal/chat. userId is forwarded
 * so the AI service can look up conversation history and apply per-user
 * rate limits — it is never used to authenticate the request itself; that
 * trust boundary is the X-Internal-Api-Key header (Architecture Doc §7.2).
 */
public record InternalChatRequest(Long userId, UUID conversationId, String question) {
}
