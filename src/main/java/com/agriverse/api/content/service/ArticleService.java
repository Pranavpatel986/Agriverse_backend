package com.agriverse.api.content.service;

import com.agriverse.api.ai.event.ArticlePublishedEvent;
import com.agriverse.api.analytics.entity.ReadingHistory;
import com.agriverse.api.analytics.repository.ReadingHistoryRepository;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ForbiddenException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.common.util.SlugUtil;
import com.agriverse.api.content.dto.*;
import com.agriverse.api.content.entity.*;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.content.repository.ArticleTagRepository;
import com.agriverse.api.content.repository.CategoryRepository;
import com.agriverse.api.content.repository.TagRepository;
import com.agriverse.api.identity.entity.Author;
import com.agriverse.api.identity.entity.User;
import com.agriverse.api.identity.repository.AuthorRepository;
import com.agriverse.api.identity.repository.UserRepository;
import com.agriverse.api.security.SecurityUtils;
import com.agriverse.api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Backs the Articles endpoint group (REST API Specification, Section 3). */
@Service
@RequiredArgsConstructor
public class ArticleService {

    private static final int WORDS_PER_MINUTE = 200;

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> browse(String categorySlug, String tagSlug, UUID authorId,
                                                         String sort, Pageable pageable) {
        Long tagId = null;
        if (tagSlug != null) {
            tagId = tagRepository.findBySlug(tagSlug).map(Tag::getId).orElse(-1L);
        }
        Pageable sorted = applySort(pageable, sort);
        Page<Article> page = articleRepository.browsePublished(categorySlug, authorId, tagId, sorted);
        return PageResponse.of(page, page.getContent().stream().map(this::toSummary).toList());
    }

    private Pageable applySort(Pageable pageable, String sort) {
        Sort s = "popular".equalsIgnoreCase(sort)
                ? Sort.by(Sort.Direction.DESC, "viewCount")
                : Sort.by(Sort.Direction.DESC, "publishedAt");
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), s);
    }

    /**
     * Public, unauthenticated-friendly lookup -- but PUBLISHED-only for
     * everyone except the owning Author or an Editor/Admin. Returns 404
     * (not 403) for a non-published article to a non-privileged caller, so
     * the response doesn't reveal that a draft with this slug exists at
     * all. View-count/reading-history tracking only fires for genuine
     * published reads, not owner/editor previews of unpublished work.
     */
    @Transactional
    public ArticleDetailResponse getBySlug(String slug) {
        Article article = articleRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", slug));

        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            if (!canPreviewUnpublished(article)) {
                throw ResourceNotFoundException.of("Article", slug);
            }
        } else {
            articleRepository.incrementViewCount(article.getId());
            recordReadingHistoryIfAuthenticated(article);
        }

        return toDetail(article);
    }

    /**
     * Full detail for the edit form, regardless of status -- gated to the
     * owning Author or an Editor/Admin via {@link #assertCanEdit}, the same
     * rule PUT/PATCH already enforce, so "can view for editing" and "can
     * actually edit" never drift apart.
     */
    @Transactional(readOnly = true)
    public ArticleEditResponse getForEdit(UUID id) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", id));
        assertCanEdit(article);

        List<ArticleTag> links = articleTagRepository.findByArticleId(article.getId());
        List<UUID> tagIds = links.stream().map(at -> at.getTag().getPublicId()).toList();
        List<String> tagNames = links.stream().map(at -> at.getTag().getName()).toList();

        return new ArticleEditResponse(
                article.getPublicId(), article.getTitle(), article.getSubtitle(), article.getSlug(),
                article.getHeroImageUrl(), article.getBody(), article.getStatus().toWire(),
                article.getCategory().getPublicId(), article.getCategory().getName(),
                tagIds, tagNames,
                article.getAuthor().getUser().getPublicId(), article.getAuthor().getDisplayName(),
                article.getSeoMetaTitle(), article.getSeoMetaDescription(), article.getPublishedAt());
    }

    private boolean canPreviewUnpublished(Article article) {
        if (SecurityUtils.hasAnyRole("EDITOR", "ADMIN")) {
            return true;
        }
        return SecurityUtils.currentPrincipal()
                .map(p -> article.getAuthor().getUser().getPublicId().equals(p.getPublicId()))
                .orElse(false);
    }

    @Transactional
    public ArticleCreatedResponse create(CreateArticleRequest request) {
        UserPrincipal principal = SecurityUtils.requireCurrentPrincipal();
        Category category = categoryRepository.findByPublicId(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId()));
        Author author = resolveOrCreateAuthor(principal);

        Article article = new Article();
        article.setTitle(request.title());
        article.setSubtitle(request.subtitle());
        article.setSlug(uniqueSlug(request.title()));
        article.setCategory(category);
        article.setAuthor(author);
        article.setBody(request.body());
        article.setHeroImageUrl(request.heroImageUrl());
        article.setExcerpt(deriveExcerpt(request.body()));
        article.setReadingTimeMinutes(estimateReadingTime(request.body()));
        article.setStatus(ArticleStatus.DRAFT);
        articleRepository.save(article);

        applyTags(article, request.tagIds());

        return new ArticleCreatedResponse(article.getPublicId(), article.getSlug(), article.getStatus().toWire());
    }

    @Transactional
    public ArticleDetailResponse update(UUID id, UpdateArticleRequest request) {
        if (request.isEmpty()) {
            throw new BadRequestException("At least one field must be provided");
        }
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", id));
        assertCanEdit(article);

        if (request.title() != null) {
            article.setTitle(request.title());
        }
        if (request.subtitle() != null) {
            article.setSubtitle(request.subtitle());
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findByPublicId(request.categoryId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Category", request.categoryId()));
            article.setCategory(category);
        }
        if (request.body() != null) {
            article.setBody(request.body());
            article.setExcerpt(deriveExcerpt(request.body()));
            article.setReadingTimeMinutes(estimateReadingTime(request.body()));
        }
        if (request.heroImageUrl() != null) {
            article.setHeroImageUrl(request.heroImageUrl());
        }
        articleRepository.save(article);

        if (request.tagIds() != null) {
            articleTagRepository.deleteByArticleId(article.getId());
            applyTags(article, request.tagIds());
        }

        return toDetail(article);
    }

    @Transactional
    public ArticleStatusResponse updateStatus(UUID id, UpdateArticleStatusRequest request) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", id));
        ArticleStatus target;
        try {
            target = ArticleStatus.fromWire(request.status());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("status must be one of: in_review, published, archived");
        }

        boolean isEditorOrAdmin = SecurityUtils.hasAnyRole("EDITOR", "ADMIN");
        if (target == ArticleStatus.PUBLISHED && !isEditorOrAdmin) {
            throw new ForbiddenException("Only EDITOR/ADMIN may set status to 'published'");
        }
        if (!isEditorOrAdmin) {
            assertCanEdit(article);
            if (target != ArticleStatus.IN_REVIEW) {
                throw new ForbiddenException("Authors may only submit their article for review");
            }
        }
        assertValidTransition(article.getStatus(), target);

        article.setStatus(target);
        if (target == ArticleStatus.PUBLISHED) {
            article.setPublishedAt(Instant.now());
            Author author = article.getAuthor();
            author.setArticlesPublishedCount(author.getArticlesPublishedCount() + 1);
            authorRepository.save(author);
        }
        articleRepository.save(article);
        if (target == ArticleStatus.PUBLISHED) {
            // Handed off to the AI service for embedding/indexing after commit —
            // see ArticleIndexingListener. Publish must never fail or slow down
            // because indexing is slow or the AI service is unavailable.
            eventPublisher.publishEvent(new ArticlePublishedEvent(
                    article.getPublicId(), article.getTitle(), article.getSlug(), article.getBody()));
        }
        return new ArticleStatusResponse(article.getPublicId(), article.getStatus().toWire(), article.getPublishedAt());
    }

    @Transactional
    public void delete(UUID id) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", id));
        article.setDeletedAt(Instant.now());
        articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public ContentListResponse<ArticleSummaryResponse> related(UUID id, int limit) {
        Article article = articleRepository.findByPublicIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", id));
        List<Article> related = articleRepository.findRelatedByCategory(article.getCategory(), article.getId(),
                PageRequest.of(0, Math.min(limit, 10)));
        return new ContentListResponse<>(related.stream().map(this::toSummary).toList());
    }

    private void recordReadingHistoryIfAuthenticated(Article article) {
        SecurityUtils.currentPrincipal().ifPresent(principal -> {
            User user = userRepository.findById(principal.getId()).orElse(null);
            if (user != null) {
                ReadingHistory rh = new ReadingHistory();
                rh.setUser(user);
                rh.setArticle(article);
                readingHistoryRepository.save(rh);
            }
        });
    }

    private Author resolveOrCreateAuthor(UserPrincipal principal) {
        return authorRepository.findByUserId(principal.getId()).orElseGet(() -> {
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> ResourceNotFoundException.of("User", principal.getId()));
            Author author = new Author();
            author.setUser(user);
            author.setDisplayName(user.getFullName());
            author.setArticlesPublishedCount(0);
            return authorRepository.save(author);
        });
    }

    private void assertCanEdit(Article article) {
        if (SecurityUtils.hasAnyRole("EDITOR", "ADMIN")) {
            return;
        }
        UUID currentUserPublicId = SecurityUtils.currentUserPublicId();
        if (!article.getAuthor().getUser().getPublicId().equals(currentUserPublicId)) {
            throw new ForbiddenException("Authors may only modify their own articles");
        }
    }

    private void assertValidTransition(ArticleStatus from, ArticleStatus to) {
        boolean valid = switch (from) {
            case DRAFT -> to == ArticleStatus.IN_REVIEW;
            case IN_REVIEW -> to == ArticleStatus.PUBLISHED || to == ArticleStatus.DRAFT;
            case PUBLISHED -> to == ArticleStatus.ARCHIVED;
            case ARCHIVED -> false;
        };
        if (!valid) {
            throw new BadRequestException("Cannot transition article from " + from.toWire() + " to " + to.toWire());
        }
    }

    private void applyTags(Article article, Set<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Tag> tags = tagRepository.findByPublicIdIn(tagIds);
        for (Tag tag : tags) {
            ArticleTag at = new ArticleTag();
            at.setArticle(article);
            at.setTag(tag);
            articleTagRepository.save(at);
        }
    }

    private String uniqueSlug(String title) {
        String base = SlugUtil.slugify(title);
        String slug = base;
        int suffix = 2;
        while (articleRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String deriveExcerpt(com.fasterxml.jackson.databind.JsonNode body) {
        String text = extractPlainText(body);
        return text.length() > 497 ? text.substring(0, 497) + "..." : text;
    }

    private Integer estimateReadingTime(com.fasterxml.jackson.databind.JsonNode body) {
        String text = extractPlainText(body);
        int words = text.isBlank() ? 0 : text.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil((double) words / WORDS_PER_MINUTE));
    }

    private String extractPlainText(com.fasterxml.jackson.databind.JsonNode body) {
        if (body == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        com.fasterxml.jackson.databind.JsonNode blocks = body.get("blocks");
        if (blocks != null && blocks.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode block : blocks) {
                com.fasterxml.jackson.databind.JsonNode textNode = block.get("text");
                if (textNode != null) {
                    sb.append(textNode.asText()).append(' ');
                }
            }
        }
        return sb.toString().trim();
    }

    private ArticleSummaryResponse toSummary(Article a) {
        return new ArticleSummaryResponse(
                a.getPublicId(), a.getTitle(), a.getSlug(), a.getExcerpt(), a.getHeroImageUrl(),
                new CategoryRef(a.getCategory().getName(), a.getCategory().getSlug()),
                new AuthorRef(a.getAuthor().getDisplayName()),
                a.getReadingTimeMinutes(), a.getPublishedAt());
    }

    private ArticleDetailResponse toDetail(Article a) {
        List<String> tags = articleTagRepository.findByArticleId(a.getId()).stream()
                .map(at -> at.getTag().getSlug())
                .toList();
        String canonicalUrl = "https://agriverse.app/articles/" + a.getSlug();
        return new ArticleDetailResponse(
                a.getPublicId(), a.getTitle(), a.getBody(), tags,
                new CategoryRef(a.getCategory().getName(), a.getCategory().getSlug()),
                new AuthorDetailRef(a.getAuthor().getDisplayName(), a.getAuthor().getBio(), a.getAuthor().getUser().getAvatarUrl()),
                new SeoRef(
                        a.getSeoMetaTitle() != null ? a.getSeoMetaTitle() : a.getTitle(),
                        a.getSeoMetaDescription() != null ? a.getSeoMetaDescription() : a.getExcerpt(),
                        canonicalUrl));
    }
}
