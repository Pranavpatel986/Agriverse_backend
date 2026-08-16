package com.agriverse.api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard error envelope, per the REST API Specification's "Standard Error
 * Response Shape": { timestamp, status, error, requestId, details }.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String requestId,
        List<FieldErrorDto> details
) {
    public static ApiErrorResponse of(int status, String error, String requestId) {
        return new ApiErrorResponse(Instant.now(), status, error, requestId, null);
    }

    public static ApiErrorResponse of(int status, String error, String requestId, List<FieldErrorDto> details) {
        return new ApiErrorResponse(Instant.now(), status, error, requestId, details);
    }
}
