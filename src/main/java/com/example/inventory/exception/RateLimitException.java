package com.example.inventory.exception;

import org.springframework.http.HttpStatus;

public class RateLimitException extends CustomException {
    private static final String ERROR_CODE = "RATE_LIMIT_EXCEEDED";

    public RateLimitException(String message) {
        super(message, ERROR_CODE, HttpStatus.TOO_MANY_REQUESTS);
    }
}
