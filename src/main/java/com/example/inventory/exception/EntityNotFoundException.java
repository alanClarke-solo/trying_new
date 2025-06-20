package com.example.inventory.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an entity cannot be found in the database.
 * This extends CustomException to provide proper HTTP status code handling.
 */
public class EntityNotFoundException extends CustomException {
    private static final String ERROR_CODE = "ENTITY_NOT_FOUND";

    /**
     * Constructor with a simple message
     *
     * @param message the error message
     */
    public EntityNotFoundException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    /**
     * Constructor with detailed entity information
     *
     * @param entityName the name of the entity type (e.g., "Product")
     * @param fieldName the name of the field used for lookup (e.g., "id")
     * @param fieldValue the value of the field that wasn't found
     */
    public EntityNotFoundException(String entityName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", entityName, fieldName, fieldValue),
                ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    /**
     * Constructor with a cause
     *
     * @param message the error message
     * @param cause the underlying exception
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.NOT_FOUND, cause);
    }
}