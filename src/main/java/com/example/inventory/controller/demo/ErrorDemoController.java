package com.example.inventory.controller.demo;

import com.example.inventory.exception.*;
import com.example.inventory.service.WebClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Demo controller to test various error scenarios
 * This is for testing/demonstration purposes only
 */
@RestController
@RequestMapping("/api/demo/errors")
@RequiredArgsConstructor
@Slf4j
public class ErrorDemoController {

    private final WebClientService webClientService;

    @GetMapping("/not-found")
    public String testNotFound() {
        throw new ResourceNotFoundException("Resource", "id", "123");
    }

    @GetMapping("/validation")
    public String testValidation(@RequestParam String value) {
        if (value == null || value.isEmpty()) {
            throw new ValidationException("Value must not be empty");
        }
        return "Valid value: " + value;
    }

    @GetMapping("/database")
    public String testDatabase() {
        throw new DatabaseException("Error executing SQL query", new RuntimeException("DB connection failed"));
    }

    @GetMapping("/external-service")
    public String testExternalService() {
        throw new ExternalServiceException("Failed to communicate with payment service");
    }

    @GetMapping("/rate-limit")
    public String testRateLimit() {
        throw new RateLimitException("Too many requests, please try again later");
    }

    @GetMapping("/webclient")
    public Mono<String> testWebClient() {
        // This will cause a WebClient error since the URL is invalid
        return webClientService.getFromExternalService("http://non-existent-service.example.com", String.class);
    }

    @GetMapping("/generic")
    public String testGenericError() {
        throw new RuntimeException("This is a generic error");
    }
}
