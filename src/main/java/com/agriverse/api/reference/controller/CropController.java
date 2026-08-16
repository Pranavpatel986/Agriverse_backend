package com.agriverse.api.reference.controller;

import com.agriverse.api.common.config.PaginationProperties;
import com.agriverse.api.reference.dto.CropPageResponse;
import com.agriverse.api.reference.dto.CropResponse;
import com.agriverse.api.reference.service.CropService;
import com.agriverse.api.common.util.PageRequestUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Crops endpoint group — REST API Specification, Section 17. */
@RestController
@RequestMapping("/api/v1/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService cropService;

    @GetMapping
    public CropPageResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 50, 200);
        return cropService.search(q, categoryId, pageable);
    }

    @GetMapping("/{id}")
    public CropResponse getById(@PathVariable UUID id) {
        return cropService.getById(id);
    }
}
