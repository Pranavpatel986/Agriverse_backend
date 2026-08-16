package com.agriverse.api.reference.service;

import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.reference.dto.MarketPriceEntry;
import com.agriverse.api.reference.dto.MarketPricesResponse;
import com.agriverse.api.reference.entity.Crop;
import com.agriverse.api.reference.entity.MarketPrice;
import com.agriverse.api.reference.repository.CropRepository;
import com.agriverse.api.reference.repository.MarketPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Backs the Market Prices endpoint group (REST API Specification, Section 15). */
@Service
@RequiredArgsConstructor
public class MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;
    private final CropRepository cropRepository;

    @Transactional(readOnly = true)
    public MarketPricesResponse lookup(UUID cropId, String state, String marketName, LocalDate from, LocalDate to, Pageable pageable) {
        Crop crop = cropRepository.findByPublicId(cropId)
                .orElseThrow(() -> ResourceNotFoundException.of("Crop", cropId));

        LocalDate rangeTo = to != null ? to : LocalDate.now();
        LocalDate rangeFrom = from != null ? from : rangeTo.minusDays(30);

        Page<MarketPrice> page = marketPriceRepository.search(crop.getId(), state, pageable);
        List<MarketPrice> filtered = page.getContent().stream()
                .filter(mp -> !mp.getPriceDate().isBefore(rangeFrom) && !mp.getPriceDate().isAfter(rangeTo))
                .filter(mp -> marketName == null || marketName.equalsIgnoreCase(mp.getMarketName()))
                .toList();

        List<MarketPriceEntry> entries = filtered.stream()
                .map(mp -> new MarketPriceEntry(mp.getMarketName(), mp.getState(), mp.getMinPrice(), mp.getMaxPrice(),
                        mp.getModalPrice(), mp.getPriceDate(), mp.getSource()))
                .toList();

        BigDecimal latest = filtered.stream()
                .max((a, b) -> a.getPriceDate().compareTo(b.getPriceDate()))
                .map(MarketPrice::getModalPrice)
                .orElse(null);

        return new MarketPricesResponse(crop.getPublicId(), entries, latest);
    }
}
