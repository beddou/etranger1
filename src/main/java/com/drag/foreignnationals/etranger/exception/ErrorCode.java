package com.drag.foreignnationals.etranger.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND),

    ENTITY_ALREADY_EXISTS(HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),

    ACCESS_DENIED(HttpStatus.FORBIDDEN),

    DATABASE_ERROR(HttpStatus.CONFLICT),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
