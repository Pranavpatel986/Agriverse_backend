package com.agriverse.api.common.exception;

import org.springframework.http.HttpStatus;

/** 400 — request is malformed or fails a business validation rule. */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
