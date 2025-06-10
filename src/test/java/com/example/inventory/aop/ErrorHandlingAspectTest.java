package com.example.inventory.aop;

import com.example.inventory.exception.CacheException;
import com.example.inventory.exception.ResourceNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ErrorHandlingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Logger logger;

    @InjectMocks
    private ErrorLoggingAspect errorHandlingAspect;

    @BeforeEach
    void setUp() {
        // Inject mock logger using reflection
        try {
            java.lang.reflect.Field loggerField = ErrorLoggingAspect.class.getDeclaredField("log");
            loggerField.setAccessible(true);
            loggerField.set(errorHandlingAspect, logger);
        } catch (Exception e) {
            fail("Failed to set up mock logger: " + e.getMessage());
        }
    }

    @Test
    void testHandleErrors_Success() throws Throwable {
        // Arrange
        String expectedResult = "Test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);
        when(joinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));

        // Act
        Object result = errorHandlingAspect.handleErrors(joinPoint);

        // Assert
        assertEquals(expectedResult, result);
        verify(logger).debug(anyString(), any(Object.class));
        verify(logger, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    void testHandleErrors_ResourceNotFoundException() throws Throwable {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");
        when(joinPoint.proceed()).thenThrow(exception);
        when(joinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> errorHandlingAspect.handleErrors(joinPoint));
        verify(logger).error(contains("Resource not found exception"), eq(exception));
    }

    @Test
    void testHandleErrors_CacheException() throws Throwable {
        // Arrange
        CacheException exception = new CacheException("Cache error");
        when(joinPoint.proceed()).thenThrow(exception);
        when(joinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));

        // Act & Assert
        assertThrows(CacheException.class, () -> errorHandlingAspect.handleErrors(joinPoint));
        verify(logger).error(contains("Cache operation failed"), eq(exception));
    }

    @Test
    void testHandleErrors_GenericException() throws Throwable {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");
        when(joinPoint.proceed()).thenThrow(exception);
        when(joinPoint.getSignature()).thenReturn(mock(org.aspectj.lang.Signature.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> errorHandlingAspect.handleErrors(joinPoint));
        verify(logger).error(contains("Unexpected error occurred"), eq(exception));
    }
}

