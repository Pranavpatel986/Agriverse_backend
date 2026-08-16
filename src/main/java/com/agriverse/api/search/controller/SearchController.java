package com.agriverse.api.search.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.search.dto.AutocompleteResponse;
import com.agriverse.api.search.dto.SearchResponse;
import com.agriverse.api.search.dto.TrendingResponse;
import com.agriverse.api.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Search endpoint group — REST API Specification, Section 5. Public. */
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final PaginationProperties paginationProperties;

    @GetMapping("/api/v1/search")
    public SearchResponse search(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 10, paginationProperties.getMaxPageSize());
        return searchService.search(q, category, pageable);
    }

    @GetMapping("/api/v1/search/autocomplete")
    public AutocompleteResponse autocomplete(@RequestParam String q) {
        return searchService.autocomplete(q);
    }

    @GetMapping("/api/v1/search/trending")
    public TrendingResponse trending(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        return searchService.trending(Math.min(limit, 25));
    }
}
