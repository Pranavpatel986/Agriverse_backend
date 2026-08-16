package com.agriverse.api.common.exception;

import org.springframework.http.HttpStatus;

/** Base class for all handled application exceptions, each carrying its own HTTP status. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
