package com.example.inventory.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends CustomException {
    private static final String ERROR_CODE = "VALIDATION_ERROR";

    public ValidationException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }
}
