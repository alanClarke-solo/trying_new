
package com.example.inventory.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Test
    public void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.toString(), response.getBody().getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleCacheException() {
        // Arrange
        CacheException exception = new CacheException("Cache error occurred");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        // Act - CacheException is handled by the generic Exception handler
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleWebClientResponseException() {
        // Arrange
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                null,
                "Invalid request".getBytes(),
                null
        );
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWebClientResponseException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("External service error"));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), response.getBody().getStatus());
        assertEquals("EXTERNAL_SERVICE_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleResponseStatusException() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Access denied"
        );
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
        assertEquals(HttpStatus.FORBIDDEN.toString(), response.getBody().getStatus());
        assertEquals("REQUEST_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleGenericException() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }
}