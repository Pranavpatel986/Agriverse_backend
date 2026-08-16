package com.agriverse.api.reference.controller;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.reference.dto.SchemeDetailResponse;
import com.agriverse.api.reference.dto.SchemeSummaryResponse;
import com.agriverse.api.reference.service.GovernmentSchemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Government Schemes endpoint group — REST API Specification, Section 13. Public read-only. */
@RestController
@RequestMapping("/api/v1/schemes")
@RequiredArgsConstructor
public class SchemeController {

    private final GovernmentSchemeService schemeService;

    @GetMapping
    public PageResponse<SchemeSummaryResponse> search(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) UUID cropId,
            @RequestParam(required = false) String beneficiaryType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 20, 100);
        return schemeService.search(state, cropId, beneficiaryType, pageable);
    }

    @GetMapping("/{id}")
    public SchemeDetailResponse getById(@PathVariable UUID id) {
        return schemeService.getById(id);
    }
}
