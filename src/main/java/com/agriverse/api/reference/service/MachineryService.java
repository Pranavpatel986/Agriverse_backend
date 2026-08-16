package com.agriverse.api.reference.service;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.reference.dto.*;
import com.agriverse.api.reference.entity.Crop;
import com.agriverse.api.reference.entity.Machinery;
import com.agriverse.api.reference.entity.MachineryCategory;
import com.agriverse.api.reference.repository.CropRepository;
import com.agriverse.api.reference.repository.MachineryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Backs the Machinery endpoint group (REST API Specification, Section 18) and its Admin management. */
@Service
@RequiredArgsConstructor
public class MachineryService {

    private final MachineryRepository machineryRepository;
    private final CropRepository cropRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public PageResponse<MachinerySummaryResponse> search(String category, UUID cropId, Pageable pageable) {
        MachineryCategory cat = parseCategory(category);
        Long cropInternalId = cropId != null ? resolveCrop(cropId).getId() : null;
        Page<Machinery> page = machineryRepository.search(cat, cropInternalId, pageable);
        return PageResponse.of(page, page.getContent().stream().map(this::toSummary).toList());
    }

    @Transactional(readOnly = true)
    public MachineryDetailResponse getById(UUID id) {
        Machinery machinery = machineryRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Machinery", id));
        return toDetail(machinery);
    }

    @Transactional
    public IdCreatedResponse create(CreateMachineryRequest request) {
        Machinery machinery = new Machinery();
        machinery.setName(request.name());
        machinery.setCategory(parseCategory(request.category()));
        machinery.setDescription(request.description());
        machinery.setPriceRangeMin(request.priceRangeMin());
        machinery.setPriceRangeMax(request.priceRangeMax());
        if (request.applicableCropId() != null) {
            machinery.setApplicableCrop(resolveCrop(request.applicableCropId()));
        }
        if (request.articleId() != null) {
            machinery.setArticle(resolveArticle(request.articleId()));
        }
        machineryRepository.save(machinery);
        return new IdCreatedResponse(machinery.getPublicId());
    }

    @Transactional
    public MachineryDetailResponse update(UUID id, UpdateMachineryRequest request) {
        if (request.isEmpty()) {
            throw new BadRequestException("At least one field must be provided");
        }
        Machinery machinery = machineryRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Machinery", id));
        if (request.name() != null) machinery.setName(request.name());
        if (request.category() != null) machinery.setCategory(parseCategory(request.category()));
        if (request.description() != null) machinery.setDescription(request.description());
        if (request.priceRangeMin() != null) machinery.setPriceRangeMin(request.priceRangeMin());
        if (request.priceRangeMax() != null) machinery.setPriceRangeMax(request.priceRangeMax());
        if (request.applicableCropId() != null) machinery.setApplicableCrop(resolveCrop(request.applicableCropId()));
        if (request.articleId() != null) machinery.setArticle(resolveArticle(request.articleId()));
        machineryRepository.save(machinery);
        return toDetail(machinery);
    }

    @Transactional
    public void delete(UUID id) {
        Machinery machinery = machineryRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Machinery", id));
        machineryRepository.delete(machinery);
    }

    private Crop resolveCrop(UUID cropId) {
        return cropRepository.findByPublicId(cropId).orElseThrow(() -> ResourceNotFoundException.of("Crop", cropId));
    }

    private Article resolveArticle(UUID articleId) {
        return articleRepository.findByPublicIdAndDeletedAtIsNull(articleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", articleId));
    }

    private MachineryCategory parseCategory(String value) {
        if (value == null) return null;
        try {
            return MachineryCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("category must be one of: tillage, sowing, harvesting, irrigation, other");
        }
    }

    private MachinerySummaryResponse toSummary(Machinery m) {
        return new MachinerySummaryResponse(m.getPublicId(), m.getName(), m.getCategory().name().toLowerCase(),
                m.getPriceRangeMin(), m.getPriceRangeMax());
    }

    private MachineryDetailResponse toDetail(Machinery m) {
        CropRef cropRef = m.getApplicableCrop() != null
                ? new CropRef(m.getApplicableCrop().getPublicId(), m.getApplicableCrop().getName()) : null;
        ArticleRefDto articleRef = m.getArticle() != null
                ? new ArticleRefDto(m.getArticle().getPublicId(), m.getArticle().getSlug(), m.getArticle().getTitle()) : null;
        return new MachineryDetailResponse(m.getPublicId(), m.getName(), m.getCategory().name().toLowerCase(),
                m.getDescription(), m.getPriceRangeMin(), m.getPriceRangeMax(), cropRef, articleRef);
    }
}
