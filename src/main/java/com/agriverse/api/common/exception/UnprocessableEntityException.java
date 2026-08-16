package com.agriverse.api.common.exception;

import org.springframework.http.HttpStatus;

/** 422 — request is well-formed but semantically invalid. */
public class UnprocessableEntityException extends ApiException {
    public UnprocessableEntityException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
