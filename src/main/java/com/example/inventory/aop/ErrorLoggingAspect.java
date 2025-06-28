package com.example.inventory.aop;

import com.example.inventory.exception.CacheException;
import com.example.inventory.exception.ExternalServiceException;
import com.example.inventory.exception.ResourceNotFoundException;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Log4j2
public class ErrorLoggingAspect {

    /**
     * Pointcut that matches all Spring components
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)" +
            " || within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.web.bind.annotation.RestController *)" +
            " || within(@org.springframework.stereotype.Component *)")
    public void springBeanPointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Pointcut that matches all beans in the application packages
     */
    @Pointcut("within(com.example.inventory..*)")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Advice that logs methods throwing exceptions
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause = '{}' and exception = '{}'",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getCause() != null ? e.getCause() : "NULL",
                e.getMessage());
    }

    /**
     * Intercepts all methods in the service, repository, and controller layers
     * to provide centralized error handling and logging.
     *
     * @param joinPoint the join point for the intercepted method
     * @return the result of the method execution
     * @throws Throwable if an error occurs that should be propagated
     */
    @Around("execution(* com.example.inventory.service.*.*(..)) || " +
            "execution(* com.example.inventory.repository.*.*(..)) || " +
            "execution(* com.example.inventory.controller.*.*(..)) || " +
            "execution(* com.example.inventory.client.*.*(..))")
    public Object handleErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        String args = Arrays.toString(joinPoint.getArgs());

        // Log method entry with sanitized parameters to avoid logging sensitive data
        log.debug("Executing {} with args: {}", methodName, sanitizeArgs(args));

        long startTime = System.currentTimeMillis();

        try {
            // Execute the intercepted method
            Object result = joinPoint.proceed();

            // Log method exit with execution time for performance monitoring
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Completed {} in {}ms", methodName, executionTime);

            return result;
        } catch (ResourceNotFoundException e) {
            // Log not found exceptions at info level since they're expected in normal operation
            log.info("Resource not found exception in {}: {}", methodName, e.getMessage());
            throw e;
        } catch (CacheException e) {
            // Specific handling for cache errors
            log.error("Cache operation failed in {}: {}", methodName, e.getMessage(), e);
            // Cache errors are critical but the app might still function, consider fallback strategy
            logMetric("cache.error", methodName);
            throw e;
        } catch (ExternalServiceException e) {
            // Handle external service errors
            log.error("External service error in {}: {}", methodName, e.getMessage(), e);
            logMetric("external.service.error", methodName);
            throw e;
        } catch (WebClientResponseException e) {
            // Specialized handling for WebClient errors with status code details
            log.error("WebClient error in {} - Status: {}, Body: {}",
                    methodName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            logMetric("webclient.error", methodName + "." + e.getStatusCode());
            // Rethrow as our custom exception
            throw new ExternalServiceException("External service error: " + e.getMessage(), e);
        } catch (TimeoutException e) {
            // Handle timeout errors specifically
            log.error("Timeout occurred in {}: {}", methodName, e.getMessage(), e);
            logMetric("timeout", methodName);
            throw e;
        } catch (Exception e) {
            // Generic error handling for all other exceptions
            log.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            // Record error metrics for monitoring
            logMetric("error", methodName + "." + e.getClass().getSimpleName());
            throw e;
        } finally {
            // Anything that needs to be done regardless of success/failure
            log.trace("Exiting {}", methodName);
        }
    }

    /**
     * Sanitizes method arguments to prevent logging sensitive information.
     *
     * @param args the string representation of the arguments
     * @return sanitized arguments string
     */
    private String sanitizeArgs(String args) {
        // Simple implementation - in a real app, you'd scan for patterns of sensitive data
        if (args.toLowerCase().contains("password") ||
                args.toLowerCase().contains("token") ||
                args.toLowerCase().contains("secret")) {
            return "[REDACTED]";
        }

        // Truncate very long argument lists
        if (args.length() > 500) {
            return args.substring(0, 500) + "... [truncated]";
        }

        return args;
    }

    /**
     * Records metrics for monitoring and alerting.
     * In a real application, this would integrate with a metrics system like
     * Micrometer, Prometheus, or similar.
     *
     * @param metricName the name of the metric
     * @param tags additional tags/dimensions for the metric
     */
    private void logMetric(String metricName, String tags) {
        // In a real implementation, this would send to a metrics system
        log.info("METRIC: {}:{}", metricName, tags);

        // Example implementation with Micrometer (commented out)
        // Counter.builder("app.errors")
        //     .tag("type", metricName)
        //     .tag("method", tags)
        //     .register(meterRegistry)
        //     .increment();
    }


}
