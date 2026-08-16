package com.agriverse.api.content.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.content.dto.*;
import com.agriverse.api.content.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Articles endpoint group — REST API Specification, Section 3. */
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    public PageResponse<ArticleSummaryResponse> browse(
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) UUID authorId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "latest") String sort) {
        var pageable = PageRequestUtil.of(page, size, paginationProperties.getDefaultPageSize(), paginationProperties.getMaxPageSize());
        return articleService.browse(categorySlug, tag, authorId, sort, pageable);
    }

    @GetMapping("/{slug}")
    public ArticleDetailResponse getBySlug(@PathVariable String slug) {
        return articleService.getBySlug(slug);
    }

    /**
     * Full detail for the edit form, any status -- distinct path from
     * /{slug} (both are single-segment-after-base, so they must live at
     * different depths to avoid an ambiguous-mapping clash at startup).
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    public ArticleEditResponse getForEdit(@PathVariable UUID id) {
        return articleService.getForEdit(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    public ResponseEntity<ArticleCreatedResponse> create(@Valid @RequestBody CreateArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    public ArticleDetailResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateArticleRequest request) {
        return articleService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AUTHOR','EDITOR','ADMIN')")
    public ArticleStatusResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateArticleStatusRequest request) {
        return articleService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    public MessageResponse delete(@PathVariable UUID id) {
        articleService.delete(id);
        return new MessageResponse("Article deleted");
    }

    @GetMapping("/{id}/related")
    public ContentListResponse<ArticleSummaryResponse> related(@PathVariable UUID id,
                                                                 @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return articleService.related(id, Math.min(limit, 10));
    }
}
