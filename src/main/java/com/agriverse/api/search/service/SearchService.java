package com.agriverse.api.search.service;

import com.agriverse.api.analytics.entity.SearchHistory;
import com.agriverse.api.analytics.repository.SearchHistoryRepository;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.content.dto.CategoryRef;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.entity.Tag;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.content.repository.CategoryRepository;
import com.agriverse.api.content.repository.TagRepository;
import com.agriverse.api.identity.repository.UserRepository;
import com.agriverse.api.search.dto.*;
import com.agriverse.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Backs the Search endpoint group (REST API Specification, Section 5).
 * Implemented against Postgres for Phase 1 (a case-insensitive
 * {@code LIKE} match over title/excerpt), matching the response contract
 * exactly. The Software Architecture Document's target design (Section
 * 11) fronts this with Meilisearch behind Redis for typo-tolerant,
 * sub-100ms full-text search; swapping the query implementation inside
 * {@link #search} and {@link #autocomplete} for a Meilisearch client is
 * the intended upgrade path — the controller and DTOs do not change.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public SearchResponse search(String q, String category, Pageable pageable) {
        String trimmed = validateQuery(q, 200);
        long start = System.currentTimeMillis();

        Page<Article> page = category != null
                ? articleRepository.browsePublished(category, null, null, pageable)
                : articleRepository.searchByTitle(trimmed, pageable);

        List<Article> filtered = category != null
                ? page.getContent().stream().filter(a -> matches(a, trimmed)).toList()
                : page.getContent();

        List<SearchResultItem> items = filtered.stream().map(a -> new SearchResultItem(
                a.getPublicId(), a.getTitle(), a.getSlug(), a.getExcerpt(), highlight(a, trimmed),
                new CategoryRef(a.getCategory().getName(), a.getCategory().getSlug()))).toList();

        recordSearchHistory(trimmed, items.size());

        long tookMs = System.currentTimeMillis() - start;
        return new SearchResponse(items, page.getTotalElements(), tookMs);
    }

    @Transactional(readOnly = true)
    public AutocompleteResponse autocomplete(String q) {
        String trimmed = validateQuery(q, 60);
        List<AutocompleteSuggestion> suggestions = new ArrayList<>();

        tagRepository.findAll().stream()
                .filter(t -> t.getName().toLowerCase().startsWith(trimmed.toLowerCase()))
                .limit(5)
                .forEach(t -> suggestions.add(new AutocompleteSuggestion(t.getName(), "tag")));

        categoryRepository.findAll().stream()
                .filter(c -> c.getName().toLowerCase().startsWith(trimmed.toLowerCase()))
                .limit(5)
                .forEach(c -> suggestions.add(new AutocompleteSuggestion(c.getName(), "category")));

        articleRepository.searchByTitle(trimmed, PageRequest.of(0, 5)).getContent()
                .forEach(a -> suggestions.add(new AutocompleteSuggestion(a.getTitle(), "article")));

        return new AutocompleteResponse(suggestions);
    }

    @Transactional(readOnly = true)
    public TrendingResponse trending(int limit) {
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        List<TrendingTerm> terms = searchHistoryRepository.findTrending(since, PageRequest.of(0, limit)).stream()
                .map(p -> new TrendingTerm(p.getTerm(), p.getCnt()))
                .toList();
        return new TrendingResponse(terms);
    }

    private void recordSearchHistory(String query, int resultCount) {
        SearchHistory history = new SearchHistory();
        SecurityUtils.currentPrincipal().ifPresent(p -> history.setUser(userRepository.getReferenceById(p.getId())));
        history.setQueryText(query);
        history.setResultCount(resultCount);
        searchHistoryRepository.save(history);
    }

    private boolean matches(Article a, String q) {
        String lower = q.toLowerCase();
        return a.getTitle().toLowerCase().contains(lower) || (a.getExcerpt() != null && a.getExcerpt().toLowerCase().contains(lower));
    }

    private String highlight(Article a, String q) {
        String source = a.getExcerpt() != null ? a.getExcerpt() : a.getTitle();
        int idx = source.toLowerCase().indexOf(q.toLowerCase());
        if (idx < 0) {
            return source;
        }
        return source.substring(0, idx) + "<em>" + source.substring(idx, idx + q.length()) + "</em>" + source.substring(idx + q.length());
    }

    private String validateQuery(String q, int maxLength) {
        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("q is required");
        }
        String trimmed = q.trim();
        if (trimmed.length() > maxLength) {
            throw new BadRequestException("q must be 1-" + maxLength + " characters");
        }
        return trimmed;
    }
}
