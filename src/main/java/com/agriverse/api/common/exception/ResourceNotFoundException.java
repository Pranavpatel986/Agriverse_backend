package com.agriverse.api.common.exception;

import org.springframework.http.HttpStatus;

/** 404 — requested resource does not exist or has been soft-deleted. */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static ResourceNotFoundException of(String entity, Object id) {
        return new ResourceNotFoundException(entity + " not found: " + id);
    }
}
