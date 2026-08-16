package com.agriverse.api.ai.dto.internal;

import java.util.List;
import java.util.UUID;

public record InternalChatResponse(UUID conversationId, String answer, List<InternalSource> sources) {

    public record InternalSource(UUID articleId, String title, String slug) {
    }
}
