package com.agriverse.api.ai.dto;

import java.util.List;
import java.util.UUID;

/**
 * disclaimer is always populated (Software Architecture Document, Section
 * 7.2: every AI response used in a user-facing context must carry a visible
 * disclaimer). sources is empty, not null, when retrieval found nothing
 * relevant — the frontend should treat an empty list as "answered from
 * general knowledge, not grounded in our content" and render accordingly.
 */
public record AssistantAnswerResponse(
        UUID conversationId,
        String answer,
        List<SourceReference> sources,
        String disclaimer) {

    public static final String DEFAULT_DISCLAIMER =
            "This answer is AI-assisted and not a substitute for advice from a qualified agronomist or extension officer.";
}
