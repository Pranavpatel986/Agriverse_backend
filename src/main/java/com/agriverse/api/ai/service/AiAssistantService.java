package com.agriverse.api.ai.service;

import com.agriverse.api.ai.dto.AskAssistantRequest;
import com.agriverse.api.ai.dto.AssistantAnswerResponse;
import com.agriverse.api.ai.dto.SourceReference;
import com.agriverse.api.ai.dto.internal.InternalChatResponse;
import com.agriverse.api.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Backs POST /api/v1/ai/chat. Thin by design: validation lives in the DTO,
 * retrieval + generation live in the AI service, this class's only job is
 * gluing the two together and guaranteeing the disclaimer is always present
 * (Software Architecture Document §7.2 — non-negotiable for user-facing AI
 * output, so it is attached here rather than trusted from the AI service).
 */
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private final AiServiceClient aiServiceClient;

    public AssistantAnswerResponse ask(Long userId, AskAssistantRequest request) {
        InternalChatResponse response;
        try {
            response = aiServiceClient.chat(userId, request.conversationId(), request.question());
        } catch (Exception e) {
            // The real cause (bad API key, AI service down, malformed
            // request, timeout, etc.) used to be discarded here entirely —
            // the log only ever showed this class's own generic message,
            // never why. Logging it now, at ERROR since a failed chat call
            // is always worth looking at, before wrapping it in the
            // user-facing message (which stays generic on purpose — the
            // person chatting shouldn't see internal error detail).
            log.error("AI chat request failed for userId={}: {}", userId, e.toString(), e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI assistant is temporarily unavailable. Please try again shortly.");
        }

        var sources = response.sources().stream()
                .map(s -> new SourceReference(s.articleId(), s.title(), s.slug()))
                .toList();

        return new AssistantAnswerResponse(
                response.conversationId(),
                response.answer(),
                sources,
                AssistantAnswerResponse.DEFAULT_DISCLAIMER);
    }
}
