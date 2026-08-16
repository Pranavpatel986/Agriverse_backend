package com.agriverse.api.reference.controller;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.reference.dto.DiseaseDetailResponse;
import com.agriverse.api.reference.dto.DiseaseSummaryResponse;
import com.agriverse.api.reference.service.PlantDiseaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Plant Diseases endpoint group — REST API Specification, Section 14. Public read-only. */
@RestController
@RequestMapping("/api/v1/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final PlantDiseaseService diseaseService;

    @GetMapping
    public PageResponse<DiseaseSummaryResponse> search(
            @RequestParam(required = false) String symptom,
            @RequestParam(required = false) UUID cropId,
            @RequestParam(required = false) String pathogenType,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 20, 100);
        return diseaseService.search(symptom, cropId, pathogenType, severity, pageable);
    }

    @GetMapping("/{id}")
    public DiseaseDetailResponse getById(@PathVariable UUID id) {
        return diseaseService.getById(id);
    }
}
