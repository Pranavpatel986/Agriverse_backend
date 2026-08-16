package com.agriverse.api.reference.service;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.reference.dto.*;
import com.agriverse.api.reference.entity.BeneficiaryType;
import com.agriverse.api.reference.entity.Crop;
import com.agriverse.api.reference.entity.GovernmentScheme;
import com.agriverse.api.reference.repository.CropRepository;
import com.agriverse.api.reference.repository.GovernmentSchemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Backs the Government Schemes endpoint group (REST API Specification, Section 13) and its Admin management. */
@Service
@RequiredArgsConstructor
public class GovernmentSchemeService {

    private final GovernmentSchemeRepository schemeRepository;
    private final CropRepository cropRepository;

    @Transactional(readOnly = true)
    public PageResponse<SchemeSummaryResponse> search(String state, UUID cropId, String beneficiaryType, Pageable pageable) {
        Long cropInternalId = cropId != null ? resolveCropId(cropId) : null;
        BeneficiaryType type = parseBeneficiaryType(beneficiaryType);
        Page<GovernmentScheme> page = schemeRepository.search(state, cropInternalId, type, pageable);
        return PageResponse.of(page, page.getContent().stream().map(this::toSummary).toList());
    }

    @Transactional(readOnly = true)
    public SchemeDetailResponse getById(UUID id) {
        GovernmentScheme scheme = schemeRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Scheme", id));
        return toDetail(scheme);
    }

    @Transactional
    public IdCreatedResponse create(CreateSchemeRequest request) {
        Crop crop = request.cropId() != null ? resolveCrop(request.cropId()) : null;
        GovernmentScheme scheme = new GovernmentScheme();
        scheme.setName(request.name());
        scheme.setDescription(request.description());
        scheme.setBeneficiaryType(parseBeneficiaryType(request.beneficiaryType()));
        scheme.setState(request.state());
        scheme.setCrop(crop);
        scheme.setBenefitSummary(request.benefitSummary());
        scheme.setApplicationDeadline(request.applicationDeadline());
        scheme.setOfficialUrl(request.officialUrl());
        scheme.setSource(request.source());
        scheme.setLastVerifiedAt(Instant.now());
        schemeRepository.save(scheme);
        return new IdCreatedResponse(scheme.getPublicId());
    }

    @Transactional
    public SchemeDetailResponse update(UUID id, UpdateSchemeRequest request) {
        if (request.isEmpty()) {
            throw new BadRequestException("At least one field must be provided");
        }
        GovernmentScheme scheme = schemeRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Scheme", id));
        if (request.name() != null) scheme.setName(request.name());
        if (request.description() != null) scheme.setDescription(request.description());
        if (request.beneficiaryType() != null) scheme.setBeneficiaryType(parseBeneficiaryType(request.beneficiaryType()));
        if (request.state() != null) scheme.setState(request.state());
        if (request.cropId() != null) scheme.setCrop(resolveCrop(request.cropId()));
        if (request.benefitSummary() != null) scheme.setBenefitSummary(request.benefitSummary());
        if (request.applicationDeadline() != null) scheme.setApplicationDeadline(request.applicationDeadline());
        if (request.officialUrl() != null) scheme.setOfficialUrl(request.officialUrl());
        if (request.source() != null) scheme.setSource(request.source());
        scheme.setLastVerifiedAt(Instant.now());
        schemeRepository.save(scheme);
        return toDetail(scheme);
    }

    @Transactional
    public void delete(UUID id) {
        GovernmentScheme scheme = schemeRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Scheme", id));
        schemeRepository.delete(scheme);
    }

    private Long resolveCropId(UUID cropId) {
        return resolveCrop(cropId).getId();
    }

    private Crop resolveCrop(UUID cropId) {
        return cropRepository.findByPublicId(cropId)
                .orElseThrow(() -> ResourceNotFoundException.of("Crop", cropId));
    }

    private BeneficiaryType parseBeneficiaryType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return BeneficiaryType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("beneficiaryType must be one of: farmer, student, cooperative, other");
        }
    }

    private SchemeSummaryResponse toSummary(GovernmentScheme s) {
        return new SchemeSummaryResponse(s.getPublicId(), s.getName(), s.getBeneficiaryType().name().toLowerCase(),
                s.getState(), s.getBenefitSummary(), s.getApplicationDeadline());
    }

    private SchemeDetailResponse toDetail(GovernmentScheme s) {
        CropRef cropRef = s.getCrop() != null ? new CropRef(s.getCrop().getPublicId(), s.getCrop().getName()) : null;
        return new SchemeDetailResponse(s.getPublicId(), s.getName(), s.getDescription(),
                s.getBeneficiaryType().name().toLowerCase(), s.getState(), cropRef, s.getBenefitSummary(),
                s.getApplicationDeadline(), s.getOfficialUrl(), s.getSource(), s.getLastVerifiedAt());
    }
}
