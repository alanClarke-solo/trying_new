package com.example.inventory.controller.advice;

import com.example.inventory.exception.ErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Controller advice that adds additional formatting to error responses
 * This is especially useful for adding headers or modifying the response
 * for specific error types before they are sent to the client
 */
@ControllerAdvice
public class ErrorFormattingControllerAdvice implements ResponseBodyAdvice<ErrorResponse> {

    @Override
    public boolean supports(MethodParameter returnType, 
                           Class<? extends HttpMessageConverter<?>> converterType) {
        return ErrorResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public ErrorResponse beforeBodyWrite(ErrorResponse body, 
                                        MethodParameter returnType,
                                        MediaType selectedContentType, 
                                        Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                        ServerHttpRequest request, 
                                        ServerHttpResponse response) {
        
        // Add security headers to error responses
        response.getHeaders().add("X-Content-Type-Options", "nosniff");
        response.getHeaders().add("X-Frame-Options", "DENY");
        
        // Check if it's a 5xx error and add a user-friendly message
        if (body.getStatus() != null && body.getStatus().startsWith("5")) {
            response.setStatusCode(HttpStatus.valueOf(Integer.parseInt(body.getStatus().split(" ")[0])));
            
            // If internal server error, ensure a generic message is shown to the user
            if (body.getStatus().startsWith("500")) {
                body.setMessage("The server encountered an internal error. " +
                               "Our team has been notified and is working to resolve the issue.");
            }
        }
        
        return body;
    }
}
