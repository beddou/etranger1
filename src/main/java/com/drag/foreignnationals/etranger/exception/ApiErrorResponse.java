package com.drag.foreignnationals.etranger.exception;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Data
@Builder

public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private ErrorCode errorCode;
    private String message;
    private String path;
    private List<FieldErrorDetail> fieldErrors;

}
