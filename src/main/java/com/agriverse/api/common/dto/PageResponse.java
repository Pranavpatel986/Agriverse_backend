package com.agriverse.api.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standard paginated list envelope used across every list endpoint, per the
 * REST API Specification's Common Conventions:
 * { content, totalElements, totalPages, page }.
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }

    public static <T, R> PageResponse<R> of(Page<T> page, List<R> mappedContent) {
        return new PageResponse<>(mappedContent, page.getTotalElements(), page.getTotalPages(), page.getNumber());
    }
}
