package com.agriverse.api.reference.controller;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.reference.dto.MachineryDetailResponse;
import com.agriverse.api.reference.dto.MachinerySummaryResponse;
import com.agriverse.api.reference.service.MachineryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Machinery endpoint group — REST API Specification, Section 18. Public read-only. */
@RestController
@RequestMapping("/api/v1/machinery")
@RequiredArgsConstructor
public class MachineryController {

    private final MachineryService machineryService;

    @GetMapping
    public PageResponse<MachinerySummaryResponse> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID cropId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var pageable = PageRequestUtil.of(page, size, 20, 100);
        return machineryService.search(category, cropId, pageable);
    }

    @GetMapping("/{id}")
    public MachineryDetailResponse getById(@PathVariable UUID id) {
        return machineryService.getById(id);
    }
}
