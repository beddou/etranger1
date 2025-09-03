package com.drag.foreignnationals.etranger.exception;

import lombok.*;

@Data
@AllArgsConstructor

public class FieldErrorDetail {
    private String field;
    private String message;


}
