package com.agriverse.api.common.util;

import com.agriverse.api.common.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Builds a validated, clamped Pageable per the API spec's Common Conventions (zero-indexed page/size). */
public final class PageRequestUtil {

    private PageRequestUtil() {
    }

    public static Pageable of(Integer page, Integer size, int defaultSize, int maxSize) {
        int p = page == null ? 0 : page;
        int s = size == null ? defaultSize : size;
        if (p < 0) {
            throw new BadRequestException("page must be >= 0");
        }
        if (s < 1 || s > maxSize) {
            throw new BadRequestException("size must be between 1 and " + maxSize);
        }
        return PageRequest.of(p, s);
    }

    public static Pageable of(Integer page, Integer size, int defaultSize, int maxSize, Sort sort) {
        int p = page == null ? 0 : page;
        int s = size == null ? defaultSize : size;
        if (p < 0) {
            throw new BadRequestException("page must be >= 0");
        }
        if (s < 1 || s > maxSize) {
            throw new BadRequestException("size must be between 1 and " + maxSize);
        }
        return PageRequest.of(p, s, sort);
    }
}
