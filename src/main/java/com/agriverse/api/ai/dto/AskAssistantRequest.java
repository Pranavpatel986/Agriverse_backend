package com.agriverse.api.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * POST /api/v1/ai/chat body. conversationId is optional — omit it to start
 * a new conversation, pass it back to continue one (history is kept by the
 * AI service, keyed by conversationId + the authenticated user).
 */
public record AskAssistantRequest(
        @NotBlank @Size(max = 1000) String question,
        UUID conversationId) {
}
