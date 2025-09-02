package com.drag.foreignnationals.etranger.exception;

import lombok.*;

public class FieldErrorDetail {
    private String field;
    private String message;

    public FieldErrorDetail(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
