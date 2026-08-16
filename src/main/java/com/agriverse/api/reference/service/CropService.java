package com.agriverse.api.reference.service;

import com.agriverse.api.common.exception.BadRequestException;
import com.agriverse.api.common.exception.ConflictException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.content.entity.Category;
import com.agriverse.api.content.repository.CategoryRepository;
import com.agriverse.api.reference.dto.CreateCropRequest;
import com.agriverse.api.reference.dto.CropPageResponse;
import com.agriverse.api.reference.dto.CropResponse;
import com.agriverse.api.reference.dto.UpdateCropRequest;
import com.agriverse.api.reference.entity.Crop;
import com.agriverse.api.reference.repository.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Backs the Crops endpoint group (REST API Specification, Section 17) and its Admin management. */
@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public CropPageResponse search(String q, UUID categoryId, Pageable pageable) {
        Page<Crop> page = cropRepository.search(q, pageable);
        var content = page.getContent().stream()
                .filter(c -> categoryId == null || (c.getCategory() != null && c.getCategory().getPublicId().equals(categoryId)))
                .map(this::toResponse)
                .toList();
        return new CropPageResponse(content, page.getTotalElements(), page.getNumber());
    }

    @Transactional(readOnly = true)
    public CropResponse getById(UUID id) {
        Crop crop = cropRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Crop", id));
        return toResponse(crop);
    }

    @Transactional
    public CropResponse create(CreateCropRequest request) {
        if (cropRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("A crop named '" + request.name() + "' already exists");
        }
        Crop crop = new Crop();
        crop.setName(request.name());
        crop.setScientificName(request.scientificName());
        if (request.categoryId() != null) {
            crop.setCategory(resolveCategory(request.categoryId()));
        }
        cropRepository.save(crop);
        return toResponse(crop);
    }

    @Transactional
    public CropResponse update(UUID id, UpdateCropRequest request) {
        if (request.isEmpty()) {
            throw new BadRequestException("At least one field must be provided");
        }
        Crop crop = cropRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Crop", id));

        if (request.name() != null && !request.name().equalsIgnoreCase(crop.getName())
                && cropRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("A crop named '" + request.name() + "' already exists");
        }

        if (request.name() != null) {
            crop.setName(request.name());
        }
        if (request.scientificName() != null) {
            crop.setScientificName(request.scientificName());
        }
        if (request.categoryId() != null) {
            crop.setCategory(resolveCategory(request.categoryId()));
        }
        cropRepository.save(crop);
        return toResponse(crop);
    }

    @Transactional
    public void delete(UUID id) {
        Crop crop = cropRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Crop", id));
        // Deliberately no cascading delete: Government Schemes, Machinery, Market Prices, and
        // Plant Disease links can all reference a crop. Let the FK constraint surface as a
        // clear 409 rather than silently orphaning or cascading through unrelated domains.
        try {
            cropRepository.delete(crop);
            cropRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException("Crop is referenced by other records (schemes, machinery, market prices, or disease entries) and cannot be deleted");
        }
    }

    private Category resolveCategory(UUID categoryId) {
        return categoryRepository.findByPublicId(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
    }

    private CropResponse toResponse(Crop crop) {
        return new CropResponse(crop.getPublicId(), crop.getName(), crop.getScientificName(),
                crop.getCategory() != null ? crop.getCategory().getPublicId() : null);
    }
}
