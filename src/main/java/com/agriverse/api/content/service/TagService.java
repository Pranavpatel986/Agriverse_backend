package com.agriverse.api.content.service;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.exception.ConflictException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.common.util.SlugUtil;
import com.agriverse.api.content.dto.CreateTagRequest;
import com.agriverse.api.content.dto.TagResponse;
import com.agriverse.api.content.dto.UpdateTagRequest;
import com.agriverse.api.content.entity.Tag;
import com.agriverse.api.content.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Backs Tags: a public listing endpoint (feeds the article editor's tag
 * picker) plus EDITOR/ADMIN management. Not part of the REST API
 * Specification's original 18 endpoint groups -- the spec assumes tags are
 * referenced by ID on article create/update but never defines how a tag
 * comes to exist in the first place. Added to close that gap using the
 * same shape as every other reference-data domain.
 */
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public ContentListResponse<TagResponse> list() {
        return new ContentListResponse<>(tagRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList());
    }

    @Transactional
    public TagResponse create(CreateTagRequest request) {
        if (tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("A tag named '" + request.name() + "' already exists");
        }
        Tag tag = new Tag();
        tag.setName(request.name());
        tag.setSlug(uniqueSlug(request.name()));
        tagRepository.save(tag);
        return toResponse(tag);
    }

    @Transactional
    public TagResponse update(UUID id, UpdateTagRequest request) {
        Tag tag = tagRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Tag", id));
        if (!request.name().equalsIgnoreCase(tag.getName()) && tagRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("A tag named '" + request.name() + "' already exists");
        }
        tag.setName(request.name());
        tag.setSlug(uniqueSlugExcluding(request.name(), tag.getId()));
        tagRepository.save(tag);
        return toResponse(tag);
    }

    @Transactional
    public void delete(UUID id) {
        Tag tag = tagRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Tag", id));
        // Article_Tags rows referencing this tag are left for the FK constraint to catch --
        // same "surface a clear 409 rather than silently cascading" choice as Crop deletion.
        try {
            tagRepository.delete(tag);
            tagRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new ConflictException("Tag is still applied to one or more articles and cannot be deleted");
        }
    }

    private String uniqueSlug(String name) {
        String base = SlugUtil.slugify(name);
        String slug = base;
        int suffix = 2;
        while (tagRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String uniqueSlugExcluding(String name, Long excludeId) {
        String base = SlugUtil.slugify(name);
        String slug = base;
        int suffix = 2;
        while (tagRepository.findBySlug(slug).filter(t -> !t.getId().equals(excludeId)).isPresent()) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getPublicId(), tag.getName(), tag.getSlug());
    }
}
