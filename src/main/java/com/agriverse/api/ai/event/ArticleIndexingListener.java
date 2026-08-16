package com.agriverse.api.ai.event;

import com.agriverse.api.ai.dto.internal.InternalIndexArticleRequest;
import com.agriverse.api.ai.service.AiServiceClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fires the indexing call to the AI service after the publish transaction
 * commits (TransactionalEventListener, AFTER_COMMIT — indexing an article
 * that then rolls back would poison the vector index with content that
 * doesn't exist). Runs @Async so a slow or down AI service can never slow
 * down or fail the publish request itself; failures are logged, not
 * retried here — the AI service's own periodic re-index job (see
 * ai-service/README) is the backstop for anything missed.
 *
 * Requires @EnableAsync on a @Configuration class somewhere in the app if
 * one isn't already present.
 */
@Component
@RequiredArgsConstructor
public class ArticleIndexingListener {

    private static final Logger log = LoggerFactory.getLogger(ArticleIndexingListener.class);

    private final AiServiceClient aiServiceClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onArticlePublished(ArticlePublishedEvent event) {
        try {
            aiServiceClient.indexArticle(new InternalIndexArticleRequest(
                    event.articleId(), event.title(), event.slug(), event.body()));
        } catch (Exception e) {
            log.warn("Failed to index articleId={} into AI service: {}", event.articleId(), e.toString());
        }
    }
}
