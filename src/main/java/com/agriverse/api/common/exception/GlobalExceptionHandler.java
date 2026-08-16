package com.agriverse.api.common.exception;

import com.agriverse.api.common.dto.ApiErrorResponse;
import com.agriverse.api.common.dto.FieldErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

/**
 * Translates every exception into the single error envelope defined by the
 * REST API Specification's "Standard Error Response Shape", so clients only
 * ever need one generic error handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        String requestId = requestId(request);
        if (ex.getStatus().is5xxServerError()) {
            log.error("[{}] Unhandled API exception at {}", requestId, request.getRequestURI(), ex);
        } else {
            log.warn("[{}] {} at {}: {}", requestId, ex.getStatus(), request.getRequestURI(), ex.getMessage());
        }
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(ex.getStatus());
        if (ex instanceof RateLimitExceededException rle) {
            builder.header(HttpHeaders.RETRY_AFTER, String.valueOf(rle.getRetryAfterSeconds()));
        }
        return builder.body(ApiErrorResponse.of(ex.getStatus().value(), ex.getStatus().getReasonPhrase(), requestId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDto> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        String requestId = requestId(request);
        log.warn("[{}] Validation failed at {}: {}", requestId, request.getRequestURI(), details);
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(400, "Validation Failed", requestId, details));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String requestId = requestId(request);
        List<FieldErrorDto> details = List.of(new FieldErrorDto(ex.getName(), "must be a valid " + typeName(ex)));
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(400, "Validation Failed", requestId, details));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String requestId = requestId(request);
        log.warn("[{}] Access denied at {}: {}", requestId, request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(403, "Forbidden", requestId));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        String requestId = requestId(request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(401, "Invalid email or password", requestId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        String requestId = requestId(request);
        log.error("[{}] Unexpected error at {}", requestId, request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(500, "Internal Server Error", requestId));
    }

    private FieldErrorDto toFieldError(FieldError fe) {
        return new FieldErrorDto(fe.getField(), fe.getDefaultMessage());
    }

    private String typeName(MethodArgumentTypeMismatchException ex) {
        return ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "value";
    }

    private String requestId(HttpServletRequest request) {
        Object attr = request.getAttribute("requestId");
        return attr != null ? attr.toString() : UUID.randomUUID().toString();
    }
}
