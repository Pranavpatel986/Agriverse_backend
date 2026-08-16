package com.agriverse.api.content.controller;

import com.agriverse.api.common.dto.ContentListResponse;
import com.agriverse.api.common.dto.MessageResponse;
import com.agriverse.api.content.dto.*;
import com.agriverse.api.content.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Categories endpoint group — REST API Specification, Section 4. */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ContentListResponse<CategoryTreeNode> getTree() {
        return categoryService.getTree();
    }

    @GetMapping("/{slug}")
    public CategoryDetailResponse getBySlug(@PathVariable String slug) {
        return categoryService.getBySlug(slug);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryCreatedResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDetailResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponse delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return new MessageResponse("Category deleted");
    }
}
