package com.agriverse.api.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code agriverse.ai.*} — the internal contract between the Spring
 * Boot API and the FastAPI AI service described in the Software
 * Architecture Document, Section 7.2. The AI service is never reachable
 * from the frontend directly; every call is made server-to-server using
 * {@code internalApiKey}, and the AI service trusts the caller rather than
 * re-authenticating the end user.
 */
@ConfigurationProperties(prefix = "agriverse.ai")
public class AiServiceProperties {

    /** Base URL of the FastAPI AI service, e.g. http://ai-service:8000 */
    private String baseUrl = "http://localhost:8000";

    /** Shared secret sent as X-Internal-Api-Key on every call to the AI service. */
    private String internalApiKey = "change-this-in-production";

    private int connectTimeoutMs = 2000;

    /** Chat calls involve LLM generation and need a longer read timeout than other calls. */
    private int chatReadTimeoutMs = 20000;

    private int defaultReadTimeoutMs = 5000;

    /** Feature flag: when false, RecommendationService never calls the AI service. */
    private boolean semanticRecommendationsEnabled = false;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getInternalApiKey() {
        return internalApiKey;
    }

    public void setInternalApiKey(String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getChatReadTimeoutMs() {
        return chatReadTimeoutMs;
    }

    public void setChatReadTimeoutMs(int chatReadTimeoutMs) {
        this.chatReadTimeoutMs = chatReadTimeoutMs;
    }

    public int getDefaultReadTimeoutMs() {
        return defaultReadTimeoutMs;
    }

    public void setDefaultReadTimeoutMs(int defaultReadTimeoutMs) {
        this.defaultReadTimeoutMs = defaultReadTimeoutMs;
    }

    public boolean isSemanticRecommendationsEnabled() {
        return semanticRecommendationsEnabled;
    }

    public void setSemanticRecommendationsEnabled(boolean semanticRecommendationsEnabled) {
        this.semanticRecommendationsEnabled = semanticRecommendationsEnabled;
    }
}
