package com.example.inventory.exception;

/**
 * Exception thrown when Redis cache operations fail.
 * This is part of the error handling subsystem to handle
 * cache-specific exceptions.
 */
public class CacheException extends RuntimeException {

    /**
     * Creates a new CacheException with the specified message.
     *
     * @param message the detail message
     */
    public CacheException(String message) {
        super(message);
    }

    /**
     * Creates a new CacheException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}