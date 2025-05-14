package com.example.inventory.exception;

import org.springframework.http.HttpStatus;

public class DatabaseException extends CustomException {
    private static final String ERROR_CODE = "DATABASE_ERROR";

    public DatabaseException(String message) {
        super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
