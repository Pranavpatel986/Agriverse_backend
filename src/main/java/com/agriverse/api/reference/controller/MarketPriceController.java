package com.agriverse.api.reference.controller;

import com.agriverse.api.common.util.PageRequestUtil;
import com.agriverse.api.reference.dto.MarketPricesResponse;
import com.agriverse.api.reference.service.MarketPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/** Market Prices endpoint group — REST API Specification, Section 15. Public read-only. */
@RestController
@RequestMapping("/api/v1/market-prices")
@RequiredArgsConstructor
public class MarketPriceController {

    private final MarketPriceService marketPriceService;

    @GetMapping
    public MarketPricesResponse lookup(
            @RequestParam UUID cropId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String marketName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var pageable = PageRequestUtil.of(0, 200, 200, 500);
        return marketPriceService.lookup(cropId, state, marketName, from, to, pageable);
    }
}
