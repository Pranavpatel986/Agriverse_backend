package com.agriverse.api.content.service;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.exception.ConflictException;
import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.common.util.SlugUtil;
import com.agriverse.api.content.dto.*;
import com.agriverse.api.content.entity.Category;
import com.agriverse.api.content.repository.ArticleRepository;
import com.agriverse.api.content.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Backs the Categories endpoint group (REST API Specification, Section 4). */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public ContentListResponse<CategoryTreeNode> getTree() {
        List<Category> roots = categoryRepository.findByParentCategoryIsNullOrderByDisplayOrderAsc();
        return new ContentListResponse<>(roots.stream().map(this::toTreeNode).toList());
    }

    @Transactional(readOnly = true)
    public CategoryDetailResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + slug));
        long articleCount = articleRepository.browsePublished(category.getSlug(), null, null,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        List<Category> children = categoryRepository.findByParentCategoryIdOrderByDisplayOrderAsc(category.getId());
        return new CategoryDetailResponse(category.getPublicId(), category.getName(), category.getDescription(),
                category.getParentCategory() != null ? category.getParentCategory().getPublicId() : null,
                articleCount, children.stream().map(this::toTreeNode).toList());
    }

    @Transactional
    public CategoryCreatedResponse create(CreateCategoryRequest request) {
        Category parent = resolveParent(request.parentCategoryId());
        String slug = uniqueSlug(request.name());
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(slug);
        category.setParentCategory(parent);
        category.setDescription(request.description());
        categoryRepository.save(category);
        return new CategoryCreatedResponse(category.getPublicId(), category.getSlug());
    }

    @Transactional
    public CategoryDetailResponse update(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", id));
        if (request.name() != null) {
            category.setName(request.name());
            category.setSlug(uniqueSlugExcluding(request.name(), category.getId()));
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        if (request.parentCategoryId() != null) {
            Category newParent = resolveParent(request.parentCategoryId());
            assertNoCycle(category, newParent);
            category.setParentCategory(newParent);
        }
        categoryRepository.save(category);
        long articleCount = articleRepository.browsePublished(category.getSlug(), null, null,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        List<Category> children = categoryRepository.findByParentCategoryIdOrderByDisplayOrderAsc(category.getId());
        return new CategoryDetailResponse(category.getPublicId(), category.getName(), category.getDescription(),
                category.getParentCategory() != null ? category.getParentCategory().getPublicId() : null,
                articleCount, children.stream().map(this::toTreeNode).toList());
    }

    @Transactional
    public void delete(UUID id) {
        Category category = categoryRepository.findByPublicId(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", id));
        if (categoryRepository.countByParentCategoryId(category.getId()) > 0) {
            throw new ConflictException("Category has articles or subcategories and cannot be deleted");
        }
        long articleCount = articleRepository.browsePublished(category.getSlug(), null, null,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        if (articleCount > 0) {
            throw new ConflictException("Category has articles or subcategories and cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    private Category resolveParent(UUID parentCategoryId) {
        if (parentCategoryId == null) {
            return null;
        }
        return categoryRepository.findByPublicId(parentCategoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Parent category", parentCategoryId));
    }

    private void assertNoCycle(Category category, Category candidateParent) {
        Category cursor = candidateParent;
        while (cursor != null) {
            if (cursor.getId().equals(category.getId())) {
                throw new ConflictException("A category cannot be set as its own ancestor");
            }
            cursor = cursor.getParentCategory();
        }
    }

    private String uniqueSlug(String name) {
        String base = SlugUtil.slugify(name);
        String slug = base;
        int suffix = 2;
        while (categoryRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String uniqueSlugExcluding(String name, Long excludeId) {
        String base = SlugUtil.slugify(name);
        String slug = base;
        int suffix = 2;
        while (categoryRepository.findBySlug(slug).filter(c -> !c.getId().equals(excludeId)).isPresent()) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private CategoryTreeNode toTreeNode(Category category) {
        List<Category> children = categoryRepository.findByParentCategoryIdOrderByDisplayOrderAsc(category.getId());
        return new CategoryTreeNode(category.getPublicId(), category.getName(), category.getSlug(),
                children.stream().map(this::toTreeNode).toList());
    }
}
