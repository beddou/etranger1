package com.drag.foreignnationals.etranger.exception;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private ErrorCode errorCode;
    private String message;
    private String path;
    private List<FieldErrorDetail> fieldErrors;

    public ApiErrorResponse(LocalDateTime timestamp, int status, ErrorCode errorCode, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    public ApiErrorResponse(LocalDateTime timestamp, int status, ErrorCode errorCode, String message, String path, List<FieldErrorDetail> fieldErrors) {
        this(timestamp, status, errorCode, message, path);
        this.fieldErrors = fieldErrors;
    }



}
