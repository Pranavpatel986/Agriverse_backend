package com.agriverse.api.common.dto;

import java.util.List;

/**
 * Lightweight paginated envelope for endpoints that return only
 * {content, totalElements, page} without totalPages (e.g. reading history).
 */
public record SimplePageResponse<T>(
        List<T> content,
        long totalElements,
        int page
) {
}
