package com.agriverse.api.common.exception;

import org.springframework.http.HttpStatus;

/** 409 — request conflicts with current resource state (duplicate, invalid transition). */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
