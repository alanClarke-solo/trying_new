package com.example.inventory.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that adds a unique request ID to the MDC for request tracing in logs
 */
@Component
public class MDCFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final String REQUEST_URI_MDC_KEY = "requestURI";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Check for request ID in header or generate a new one
            String requestId = request.getHeader(REQUEST_ID_HEADER_NAME);
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }
            
            // Add the request ID to the MDC context for logging
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            
            // Add the request URI to the MDC context
            MDC.put(REQUEST_URI_MDC_KEY, request.getRequestURI());
            
            // Add the request ID to the response headers for client tracing
            response.addHeader(REQUEST_ID_HEADER_NAME, requestId);
            
            // Continue the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the MDC context after the request is processed
            MDC.remove(REQUEST_ID_MDC_KEY);
            MDC.remove(REQUEST_URI_MDC_KEY);
        }
    }
}
