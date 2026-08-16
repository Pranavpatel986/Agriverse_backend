package com.agriverse.api.admin.service;

import com.agriverse.api.admin.dto.*;
import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ConflictException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.entity.ArticleStatus;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.engagement.entity.NotificationType;
import com.agriverse.api.engagement.service.NotificationService;
import com.agriverse.api.identity.entity.Author;
import com.agriverse.api.identity.repository.AuthorRepository;
import com.agriverse.api.identity.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Backs the Admin > Articles endpoints (REST API Specification, Section 12). */
@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private final ArticleRepository articleRepository;
    private final AuthorRepository authorRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<AdminArticleSummaryResponse> list(String status, UUID authorId, Pageable pageable) {
        ArticleStatus articleStatus = status != null ? parseStatus(status) : null;
        Page<Article> page = articleRepository.adminSearch(articleStatus, authorId, pageable);
        return PageResponse.of(page, page.getContent().stream().map(this::toSummary).toList());
    }

    @Transactional
    public ArticleReviewResponse review(UUID id, ArticleReviewRequest request) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", id));
        if (article.getStatus() != ArticleStatus.IN_REVIEW) {
            throw new ConflictException("Article is not in review");
        }
        String decision = request.decision().trim().toLowerCase();
        if (!decision.equals("approve") && !decision.equals("reject")) {
            throw new BadRequestException("decision must be 'approve' or 'reject'");
        }

        if (decision.equals("approve")) {
            article.setStatus(ArticleStatus.PUBLISHED);
            article.setPublishedAt(Instant.now());
            Author author = article.getAuthor();
            author.setArticlesPublishedCount(author.getArticlesPublishedCount() + 1);
            authorRepository.save(author);
        } else {
            article.setStatus(ArticleStatus.DRAFT);
        }
        articleRepository.save(article);

        // Roadmap gap this closes: MODERATION_STATUS notifications existed as
        // a type with nothing ever creating one. The author finding out their
        // article was approved/rejected only by refreshing the dashboard
        // wasn't really "editorial workflow" so much as "editorial workflow
        // with no feedback loop."
        User authorUser = article.getAuthor().getUser();
        if (authorUser != null) {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("articleId", article.getPublicId().toString());
            payload.put("articleTitle", article.getTitle());
            payload.put("decision", decision);
            if (request.feedback() != null && !request.feedback().isBlank()) {
                payload.put("feedback", request.feedback());
            }
            notificationService.create(authorUser, NotificationType.MODERATION_STATUS, payload);
        }

        return new ArticleReviewResponse(article.getPublicId(), article.getStatus().toWire());
    }

    private ArticleStatus parseStatus(String value) {
        try {
            return ArticleStatus.fromWire(value);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("status must be one of: draft, in_review, published, archived");
        }
    }

    private AdminArticleSummaryResponse toSummary(Article a) {
        return new AdminArticleSummaryResponse(a.getPublicId(), a.getTitle(), a.getStatus().toWire(),
                new AdminAuthorRef(a.getAuthor().getDisplayName()));
    }
}
