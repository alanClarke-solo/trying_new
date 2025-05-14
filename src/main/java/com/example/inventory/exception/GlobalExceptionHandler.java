package com.example.inventory.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, WebRequest request) {
        log.error("Custom exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().toString())
                .errorCode(ex.getErrorCode())
                .message(truncateMessage(ex.getMessage()))
                .path(getRequestPath(request))
                .build();
                
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("Validation exception occurred: {}", ex.getMessage());
        
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.ValidationError(
                        error.getField(),
                        error.getDefaultMessage()))
                .collect(Collectors.toList());
                
        String errorMessage = "Validation failed for request. Check 'errors' field for details.";
                
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.toString())
                .errorCode("VALIDATION_ERROR")
                .message(errorMessage)
                .path(getRequestPath(request))
                .errors(validationErrors)
                .build();
                
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindExceptions(
            BindException ex, WebRequest request) {
        
        log.error("Bind exception occurred: {}", ex.getMessage());
        
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
                
        String errorMessage = "Invalid request parameters: " + String.join(", ", errors);
                
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.toString())
                .errorCode("INVALID_PARAMETERS")
                .message(truncateMessage(errorMessage))
                .path(getRequestPath(request))
                .build();
                
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex, WebRequest request) {
        
        log.error("Database exception occurred: {}", ex.getMessage(), ex);
        
        String errorMessage = "Database operation failed: " + ex.getMostSpecificCause().getMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .errorCode("DATABASE_ERROR")
                .message(truncateMessage(errorMessage))
                .path(getRequestPath(request))
                .build();
                
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponseException(
            WebClientResponseException ex, WebRequest request) {
        
        log.error("External service error: {} - {}", ex.getStatusCode(), ex.getMessage(), ex);
        
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        
        String errorMessage = "External service error: " + ex.getMessage();
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.toString())
                .errorCode("EXTERNAL_SERVICE_ERROR")
                .message(truncateMessage(errorMessage))
                .path(getRequestPath(request))
                .build();
                
        return new ResponseEntity<>(errorResponse, status);
    }
    
    @ExceptionHandler(WebClientException.class)
    public ResponseEntity<ErrorResponse> handleWebClientException(
            WebClientException ex, WebRequest request) {
        
        log.error("WebClient error: {}", ex.getMessage(), ex);
        
        String errorMessage = "Failed to communicate with external service";
        if (ex.getCause() instanceof ConnectException) {
            errorMessage = "Connection refused from external service, service may be unavailable";
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.toString())
                .errorCode("EXTERNAL_SERVICE_ERROR")
                .message(truncateMessage(errorMessage))
                .path(getRequestPath(request))
                .build();
                
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {
        
        log.error("Status exception: {} - {}", ex.getStatusCode(), ex.getReason(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatusCode().toString())
                .errorCode("REQUEST_ERROR")
                .message(truncateMessage(ex.getReason()))
                .path(getRequestPath(request))
                .build();
                
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .errorCode("INTERNAL_SERVER_ERROR")
                .message(truncateMessage("An unexpected error occurred. Please try again later."))
                .path(getRequestPath(request))
                .build();
                
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return null;
    }
    
    private String truncateMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= MAX_ERROR_MESSAGE_LENGTH ? 
               message : message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
