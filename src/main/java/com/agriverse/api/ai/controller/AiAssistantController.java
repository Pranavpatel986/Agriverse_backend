package com.agriverse.api.ai.controller;

import com.agriverse.api.ai.dto.AskAssistantRequest;
import com.agriverse.api.ai.dto.AssistantAnswerResponse;
import com.agriverse.api.ai.service.AiAssistantService;
import com.agriverse.api.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Assistant endpoint group. New in this addendum to the REST API
 * Specification — not present in the original v1.1 OpenAPI document.
 * Requires authentication (falls under the existing bearerAuth security
 * scheme); there is no anonymous chat access, both to rate-limit sensibly
 * per user and because conversation history is scoped to a user.
 */
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping
    public AssistantAnswerResponse ask(@Valid @RequestBody AskAssistantRequest request) {
        Long userId = SecurityUtils.currentUserId();
        return aiAssistantService.ask(userId, request);
    }
}
