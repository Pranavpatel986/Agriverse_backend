package com.agriverse.api.ai.service;

import com.agriverse.api.ai.dto.internal.InternalChatRequest;
import com.agriverse.api.ai.dto.internal.InternalChatResponse;
import com.agriverse.api.ai.dto.internal.InternalIndexArticleRequest;
import com.agriverse.api.ai.dto.internal.InternalSimilarArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

/**
 * The single seam between the Java backend and the Python AI service
 * (Architecture Doc §7.2). Every other class in this codebase that needs
 * AI functionality — AiAssistantService, RecommendationService, the
 * indexing listener — goes through here, not through RestClient directly,
 * so retry/timeout/error-handling policy lives in one place.
 *
 * Failures are swallowed at the call site (see the individual methods'
 * javadoc) rather than here, because "fall back to rule-based
 * recommendations" and "surface an error to the chat user" are different
 * policies for different callers.
 */
@Component
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestClient defaultClient;
    private final RestClient chatClient;

    public AiServiceClient(
            @Qualifier("aiServiceRestClient") RestClient defaultClient,
            @Qualifier("aiServiceChatRestClient") RestClient chatClient) {
        this.defaultClient = defaultClient;
        this.chatClient = chatClient;
    }

    /** Throws on failure — the caller (AiAssistantService) turns that into a user-facing error. */
    public InternalChatResponse chat(Long userId, UUID conversationId, String question) {
        return chatClient.post()
                .uri("/internal/chat")
                .body(new InternalChatRequest(userId, conversationId, question))
                .retrieve()
                .body(InternalChatResponse.class);
    }

    /**
     * Returns null on any failure (timeout, 5xx, service down) instead of
     * throwing — callers use this for the semantic-recommendations seam,
     * where "fall back to rule-based" is always preferable to a broken
     * recommendations rail.
     */
    public List<InternalSimilarArticle> findSimilarArticles(UUID articleId, int limit) {
        try {
            InternalSimilarArticle[] results = defaultClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/similar-articles")
                            .queryParam("articleId", articleId)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(InternalSimilarArticle[].class);
            return results == null ? List.of() : List.of(results);
        } catch (Exception e) {
            log.warn("AI service similar-articles call failed for articleId={}, falling back: {}", articleId, e.toString());
            return null;
        }
    }

    /**
     * Fire-and-forget from the caller's point of view — indexing failure
     * must never fail the publish transaction. See
     * {@link com.agriverse.api.ai.event.ArticleIndexingListener}, which
     * calls this asynchronously and only logs on failure.
     */
    public void indexArticle(InternalIndexArticleRequest request) {
        defaultClient.post()
                .uri("/internal/index")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
