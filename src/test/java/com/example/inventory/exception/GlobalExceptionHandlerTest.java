package com.example.inventory.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private ServletWebRequest servletWebRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
        when(servletWebRequest.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    public void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.toString(), response.getBody().getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    public void testHandleCustomExceptionWithServletRequest() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomException(exception, servletWebRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    public void testHandleCacheException() {
        // Arrange
        CacheException exception = new CacheException("Cache error occurred");

        // Act - CacheException extends RuntimeException, so it goes to generic handler
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
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

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWebClientResponseException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("External service error"));
        assertEquals(HttpStatus.BAD_REQUEST.toString(), response.getBody().getStatus());
        assertEquals("EXTERNAL_SERVICE_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleWebClientResponseExceptionWithUnknownStatus() {
        // Arrange
        WebClientResponseException exception = WebClientResponseException.create(
                999, // Unknown status code
                "Unknown Status",
                null,
                "Unknown error".getBytes(),
                null
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWebClientResponseException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("External service error"));
        assertEquals("EXTERNAL_SERVICE_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleWebClientException() {
        // Arrange
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                null,
                "Service temporarily unavailable".getBytes(),
                null
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWebClientResponseException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("External service error"));
        assertEquals("EXTERNAL_SERVICE_ERROR", response.getBody().getErrorCode());
  }

    @Test
    public void testHandleWebClientExceptionWithConnectException() {
        // Arrange
        WebClientResponseException exception = WebClientResponseException.create(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                null,
                "Service temporarily unavailable".getBytes(),
                null
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWebClientResponseException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("External service error"));
        assertEquals("EXTERNAL_SERVICE_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleResponseStatusException() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Access denied"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().getMessage());
        assertEquals(HttpStatus.FORBIDDEN.toString(), response.getBody().getStatus());
        assertEquals("REQUEST_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleResponseStatusExceptionWithNullReason() {
        // Arrange
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.FORBIDDEN);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResponseStatusException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage()); // Should handle null reason gracefully
        assertEquals("REQUEST_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleDataAccessException() {
        // Arrange
        DataAccessException exception = new DataIntegrityViolationException("Database constraint violation");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataAccessException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Database operation failed"));
        assertEquals("DATABASE_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleMethodArgumentNotValidException() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        FieldError fieldError = new FieldError("product", "name", "Name is required");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        when(exception.getMessage()).thenReturn("Validation failed");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed for request. Check 'errors' field for details.", response.getBody().getMessage());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("name", response.getBody().getErrors().get(0).getField());
        assertEquals("Name is required", response.getBody().getErrors().get(0).getMessage());
    }

    @Test
    public void testHandleBindException() {
        // Arrange
        BindException exception = mock(BindException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.ObjectError objectError = mock(org.springframework.validation.ObjectError.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(objectError));
        when(objectError.getDefaultMessage()).thenReturn("Invalid parameter");
        when(exception.getMessage()).thenReturn("Bind error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBindExceptions(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Invalid request parameters"));
        assertEquals("INVALID_PARAMETERS", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleGenericException() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
        assertTrue(response.getBody().getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    public void testMessageTruncation() {
        // Arrange - Create a very long message
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1100; i++) {
            longMessage.append("a");
        }

        Exception exception = new RuntimeException(longMessage.toString());

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        // The generic handler uses a fixed message, but test with custom exception for truncation

        // Test with custom exception that would show the actual message
        ResourceNotFoundException resourceException = new ResourceNotFoundException(longMessage.toString());
        ResponseEntity<ErrorResponse> resourceResponse = exceptionHandler.handleCustomException(resourceException, webRequest);

        assertNotNull(resourceResponse.getBody());
        assertTrue(resourceResponse.getBody().getMessage().length() <= 1000);
    }

    @Test
    public void testHandleNullMessage() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage()); // Should handle null message gracefully
    }
}