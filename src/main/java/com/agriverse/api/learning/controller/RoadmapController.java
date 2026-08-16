package com.agriverse.api.learning.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.learning.dto.*;
import com.agriverse.api.learning.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Roadmaps endpoint group — REST API Specification, Section 9. */
@RestController
@RequestMapping("/api/v1/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final PaginationProperties paginationProperties;

    @GetMapping
    public PageResponse<RoadmapSummaryResponse> list(
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, paginationProperties.getDefaultPageSize(), paginationProperties.getMaxPageSize());
        return roadmapService.list(categorySlug, pageable);
    }

    @GetMapping("/{slug}")
    public RoadmapDetailResponse getBySlug(@PathVariable String slug) {
        return roadmapService.getBySlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EDITOR','ADMIN')")
    public ResponseEntity<RoadmapCreatedResponse> create(@Valid @RequestBody CreateRoadmapRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roadmapService.create(request));
    }

    @GetMapping("/{id}/progress")
    public RoadmapDetailedProgressResponse getProgress(@PathVariable UUID id) {
        return roadmapService.getProgress(id);
    }

    @PatchMapping("/{id}/progress")
    public RoadmapProgressResponse updateProgress(@PathVariable UUID id, @Valid @RequestBody UpdateRoadmapProgressRequest request) {
        return roadmapService.updateProgress(id, request);
    }
}
