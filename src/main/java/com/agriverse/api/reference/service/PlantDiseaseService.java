package com.agriverse.api.reference.service;

import com.agriverse.api.common.dto.PageResponse;
import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.entity.Article;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.reference.dto.*;
import com.agriverse.api.reference.entity.*;
import com.agriverse.api.reference.repository.CropRepository;
import com.agriverse.api.reference.repository.PlantDiseaseCropRepository;
import com.agriverse.api.reference.repository.PlantDiseaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Backs the Plant Diseases endpoint group (REST API Specification, Section 14) and its Admin management. */
@Service
@RequiredArgsConstructor
public class PlantDiseaseService {

    private final PlantDiseaseRepository diseaseRepository;
    private final PlantDiseaseCropRepository diseaseCropRepository;
    private final CropRepository cropRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public PageResponse<DiseaseSummaryResponse> search(String symptom, UUID cropId, String pathogenType, String severity, Pageable pageable) {
        Long cropInternalId = cropId != null ? resolveCrop(cropId).getId() : null;
        PathogenType type = parsePathogenType(pathogenType);
        Severity sev = parseSeverity(severity);
        Page<PlantDisease> page = diseaseRepository.search(type, sev, cropInternalId, pageable);
        List<PlantDisease> filtered = symptom == null ? page.getContent()
                : page.getContent().stream().filter(d -> d.getSymptoms().toLowerCase().contains(symptom.toLowerCase())).toList();
        return PageResponse.of(page, filtered.stream().map(this::toSummary).toList());
    }

    @Transactional(readOnly = true)
    public DiseaseDetailResponse getById(UUID id) {
        PlantDisease disease = diseaseRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Disease", id));
        return toDetail(disease);
    }

    @Transactional
    public IdCreatedResponse create(CreateDiseaseRequest request) {
        List<Crop> crops = request.cropIds().stream().map(this::resolveCrop).toList();
        Article article = request.articleId() != null ? resolveArticle(request.articleId()) : null;

        PlantDisease disease = new PlantDisease();
        disease.setName(request.name());
        disease.setPathogenType(parsePathogenType(request.pathogenType()));
        disease.setSeverity(parseSeverity(request.severity()));
        disease.setSymptoms(request.symptoms());
        disease.setTreatment(request.treatment());
        disease.setPrevention(request.prevention());
        disease.setArticle(article);
        diseaseRepository.save(disease);

        for (Crop crop : crops) {
            PlantDiseaseCrop link = new PlantDiseaseCrop();
            link.setPlantDisease(disease);
            link.setCrop(crop);
            diseaseCropRepository.save(link);
        }

        return new IdCreatedResponse(disease.getPublicId());
    }

    @Transactional
    public DiseaseDetailResponse update(UUID id, UpdateDiseaseRequest request) {
        if (request.isEmpty()) {
            throw new BadRequestException("At least one field must be provided");
        }
        PlantDisease disease = diseaseRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Disease", id));

        if (request.name() != null) disease.setName(request.name());
        if (request.pathogenType() != null) disease.setPathogenType(parsePathogenType(request.pathogenType()));
        if (request.severity() != null) disease.setSeverity(parseSeverity(request.severity()));
        if (request.symptoms() != null) disease.setSymptoms(request.symptoms());
        if (request.treatment() != null) disease.setTreatment(request.treatment());
        if (request.prevention() != null) disease.setPrevention(request.prevention());
        if (request.articleId() != null) disease.setArticle(resolveArticle(request.articleId()));
        diseaseRepository.save(disease);

        if (request.cropIds() != null) {
            diseaseCropRepository.deleteByPlantDiseaseId(disease.getId());
            for (UUID cropId : request.cropIds()) {
                PlantDiseaseCrop link = new PlantDiseaseCrop();
                link.setPlantDisease(disease);
                link.setCrop(resolveCrop(cropId));
                diseaseCropRepository.save(link);
            }
        }

        return toDetail(disease);
    }

    @Transactional
    public void delete(UUID id) {
        PlantDisease disease = diseaseRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Disease", id));
        diseaseCropRepository.deleteByPlantDiseaseId(disease.getId());
        diseaseRepository.delete(disease);
    }

    private Crop resolveCrop(UUID cropId) {
        return cropRepository.findByPublicId(cropId).orElseThrow(() -> ResourceNotFoundException.of("Crop", cropId));
    }

    private Article resolveArticle(UUID articleId) {
        return articleRepository.findByPublicIdAndDeletedAtIsNull(articleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", articleId));
    }

    private PathogenType parsePathogenType(String value) {
        if (value == null) return null;
        try {
            return PathogenType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("pathogenType must be one of: fungal, bacterial, viral, pest, nutrient_deficiency, other");
        }
    }

    private Severity parseSeverity(String value) {
        if (value == null) return null;
        try {
            return Severity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("severity must be one of: low, medium, high");
        }
    }

    private List<CropRef> cropRefs(PlantDisease disease) {
        return diseaseCropRepository.findByPlantDiseaseId(disease.getId()).stream()
                .map(link -> new CropRef(link.getCrop().getPublicId(), link.getCrop().getName()))
                .toList();
    }

    private DiseaseSummaryResponse toSummary(PlantDisease d) {
        return new DiseaseSummaryResponse(d.getPublicId(), d.getName(), d.getPathogenType().name().toLowerCase(),
                d.getSeverity().name().toLowerCase(), cropRefs(d));
    }

    private DiseaseDetailResponse toDetail(PlantDisease d) {
        ArticleRefDto articleRef = d.getArticle() != null
                ? new ArticleRefDto(d.getArticle().getPublicId(), d.getArticle().getSlug(), d.getArticle().getTitle())
                : null;
        return new DiseaseDetailResponse(d.getPublicId(), d.getName(), d.getPathogenType().name().toLowerCase(),
                d.getSeverity().name().toLowerCase(), d.getSymptoms(), d.getTreatment(), d.getPrevention(),
                cropRefs(d), articleRef);
    }
}
