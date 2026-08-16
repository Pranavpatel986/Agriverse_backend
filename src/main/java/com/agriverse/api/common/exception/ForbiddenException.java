package com.agriverse.api.common.exception;

import org.springframework.http.HttpStatus;

/** 403 — authenticated but lacking the required role, permission, or ownership. */
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
