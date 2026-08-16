package com.agriverse.api.recommendation.service;

import com.agriverse.api.ai.config.AiServiceProperties;
import com.agriverse.api.ai.dto.internal.InternalSimilarArticle;
import com.agriverse.api.ai.service.AiServiceClient;
import com.agriverse.api.analytics.repository.ReadingHistoryRepository;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.dto.ArticleSummaryResponse;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.recommendation.dto.RecommendedArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Backs the Recommendations endpoint group (REST API Specification, Section
 * 11). Implements the Phase 1-2 strategy from the Software Architecture
 * Document, Section 12: reading-history-driven category overlap. The
 * Phase 3+ semantic-similarity path (an AI/embedding service) is a seam —
 * swap {@link #forUser} and {@link #forArticle} internals without touching
 * controllers.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final ArticleRepository articleRepository;
    private final AiServiceClient aiServiceClient;
    private final AiServiceProperties aiServiceProperties;

    @Transactional(readOnly = true)
    public ContentListResponse<RecommendedArticleResponse> forUser(Long userId, int limit) {
        List<Long> recentArticleIds = readingHistoryRepository.findRecentArticleIdsByUser(userId, PageRequest.of(0, 5));
        if (recentArticleIds.isEmpty()) {
            return new ContentListResponse<>(List.of());
        }

        List<RecommendedArticleResponse> results = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>(recentArticleIds);

        for (Long articleId : recentArticleIds) {
            if (results.size() >= limit) {
                break;
            }
            Article anchor = articleRepository.findById(articleId).orElse(null);
            if (anchor == null) {
                continue;
            }
            List<Article> related = articleRepository.findRelatedByCategory(anchor.getCategory(), anchor.getId(),
                    PageRequest.of(0, limit - results.size() + seen.size()));
            for (Article candidate : related) {
                if (results.size() >= limit || !seen.add(candidate.getId())) {
                    continue;
                }
                results.add(new RecommendedArticleResponse(candidate.getPublicId(), candidate.getTitle(), candidate.getSlug(),
                        "Because you read " + anchor.getTitle()));
            }
        }
        return new ContentListResponse<>(results);
    }

    @Transactional(readOnly = true)
    public ContentListResponse<ArticleSummaryResponse> forArticle(UUID articleId, int limit) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(articleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", articleId));

        List<Article> related = semanticRelated(articleId, limit)
                .orElseGet(() -> articleRepository.findRelatedByCategory(article.getCategory(), article.getId(), PageRequest.of(0, limit)));

        List<ArticleSummaryResponse> summaries = related.stream().map(a -> new ArticleSummaryResponse(
                a.getPublicId(), a.getTitle(), a.getSlug(), a.getExcerpt(), a.getHeroImageUrl(),
                new com.agriverse.api.content.dto.CategoryRef(a.getCategory().getName(), a.getCategory().getSlug()),
                new com.agriverse.api.content.dto.AuthorRef(a.getAuthor().getDisplayName()),
                a.getReadingTimeMinutes(), a.getPublishedAt())).toList();
        return new ContentListResponse<>(summaries);
    }

    /**
     * Phase 3+ semantic path (Software Architecture Document §12): asks the
     * AI service for embedding-similar articles. Returns empty when the
     * feature flag is off, the AI service call failed, or it returned no
     * results (e.g. the anchor article isn't indexed yet) — any of those,
     * forArticle falls straight back to the category-overlap path above, so
     * this can be flipped on in production with zero risk to the endpoint.
     */
    private java.util.Optional<List<Article>> semanticRelated(UUID articleId, int limit) {
        if (!aiServiceProperties.isSemanticRecommendationsEnabled()) {
            return java.util.Optional.empty();
        }
        List<InternalSimilarArticle> similar = aiServiceClient.findSimilarArticles(articleId, limit);
        if (similar == null || similar.isEmpty()) {
            return java.util.Optional.empty();
        }
        List<Article> articles = similar.stream()
                .map(s -> articleRepository.findByPublicIdAndDeletedAtIsNull(s.articleId()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        return articles.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(articles);
    }
}
