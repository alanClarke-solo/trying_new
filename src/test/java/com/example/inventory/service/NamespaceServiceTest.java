package com.example.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class NamespaceServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private NamespaceService namespaceService;
    private final String baseNamespace = "test-inventory";

    @BeforeEach
    void setUp() {
        namespaceService = new NamespaceService(redisTemplate, baseNamespace);
        // Clear any existing tenant namespace
        namespaceService.clearTenantNamespace();
    }

    @Test
    void testSetTenantNamespace() {
        // Given
        String tenantId = "tenant123";

        // When
        namespaceService.setTenantNamespace(tenantId);

        // Then
        String currentNamespace = namespaceService.getCurrentNamespace();
        assertEquals(baseNamespace + ":" + tenantId, currentNamespace);
    }

    @Test
    void testSetTenantNamespace_withNullValue() {
        // Given
        String tenantId = null;

        // When
        namespaceService.setTenantNamespace(tenantId);

        // Then
        String currentNamespace = namespaceService.getCurrentNamespace();
        assertEquals(baseNamespace, currentNamespace);
    }

    @Test
    void testSetTenantNamespace_withEmptyString() {
        // Given
        String tenantId = "";

        // When
        namespaceService.setTenantNamespace(tenantId);

        // Then
        String currentNamespace = namespaceService.getCurrentNamespace();
        assertEquals(baseNamespace, currentNamespace);
    }

    @Test
    void testClearTenantNamespace() {
        // Given
        String tenantId = "tenant123";
        namespaceService.setTenantNamespace(tenantId);
        
        // Verify tenant is set
        assertEquals(baseNamespace + ":" + tenantId, namespaceService.getCurrentNamespace());

        // When
        namespaceService.clearTenantNamespace();

        // Then
        String currentNamespace = namespaceService.getCurrentNamespace();
        assertEquals(baseNamespace, currentNamespace);
    }

    @Test
    void testGetCurrentNamespace_withoutTenant() {
        // When
        String currentNamespace = namespaceService.getCurrentNamespace();

        // Then
        assertEquals(baseNamespace, currentNamespace);
    }

    @Test
    void testGetCurrentNamespace_withTenant() {
        // Given
        String tenantId = "tenant456";
        namespaceService.setTenantNamespace(tenantId);

        // When
        String currentNamespace = namespaceService.getCurrentNamespace();

        // Then
        assertEquals(baseNamespace + ":" + tenantId, currentNamespace);
    }

    @Test
    void testGetCurrentNamespace_afterClearingTenant() {
        // Given
        String tenantId = "tenant789";
        namespaceService.setTenantNamespace(tenantId);
        namespaceService.clearTenantNamespace();

        // When
        String currentNamespace = namespaceService.getCurrentNamespace();

        // Then
        assertEquals(baseNamespace, currentNamespace);
    }

    @Test
    void testExecuteWithTenant_newTenant() {
        // Given
        String tenantId = "tenant123";
        Function<String, String> operation = namespace -> "Result for " + namespace;

        // When
        String result = namespaceService.executeWithTenant(tenantId, operation);

        // Then
        assertEquals("Result for " + baseNamespace + ":" + tenantId, result);
        // Verify tenant is cleared after execution
        assertEquals(baseNamespace, namespaceService.getCurrentNamespace());
    }

    @Test
    void testExecuteWithTenant_existingTenant() {
        // Given
        String existingTenantId = "existing-tenant";
        String newTenantId = "new-tenant";
        namespaceService.setTenantNamespace(existingTenantId);
        
        Function<String, String> operation = namespace -> "Result for " + namespace;

        // When
        String result = namespaceService.executeWithTenant(newTenantId, operation);

        // Then
        assertEquals("Result for " + baseNamespace + ":" + newTenantId, result);
        // Verify original tenant is restored after execution
        assertEquals(baseNamespace + ":" + existingTenantId, namespaceService.getCurrentNamespace());
    }

    @Test
    void testExecuteWithTenant_noExistingTenant() {
        // Given
        String tenantId = "tenant123";
        Function<String, String> operation = namespace -> "Result for " + namespace;

        // When
        String result = namespaceService.executeWithTenant(tenantId, operation);

        // Then
        assertEquals("Result for " + baseNamespace + ":" + tenantId, result);
        // Verify no tenant is set after execution
        assertEquals(baseNamespace, namespaceService.getCurrentNamespace());
    }

    @Test
    void testExecuteWithTenant_operationThrowsException() {
        // Given
        String tenantId = "tenant123";
        Function<String, String> operation = namespace -> {
            throw new RuntimeException("Test exception");
        };

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            namespaceService.executeWithTenant(tenantId, operation);
        });

        // Verify tenant is cleared even after exception
        assertEquals(baseNamespace, namespaceService.getCurrentNamespace());
    }

    @Test
    void testExecuteWithTenant_operationThrowsExceptionWithExistingTenant() {
        // Given
        String existingTenantId = "existing-tenant";
        String newTenantId = "new-tenant";
        namespaceService.setTenantNamespace(existingTenantId);
        
        Function<String, String> operation = namespace -> {
            throw new RuntimeException("Test exception");
        };

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            namespaceService.executeWithTenant(newTenantId, operation);
        });

        // Verify original tenant is restored even after exception
        assertEquals(baseNamespace + ":" + existingTenantId, namespaceService.getCurrentNamespace());
    }

    @Test
    void testExecuteWithTenant_withNullTenant() {
        // Given
        String tenantId = null;
        Function<String, String> operation = namespace -> "Result for " + namespace;

        // When
        String result = namespaceService.executeWithTenant(tenantId, operation);

        // Then
        assertEquals("Result for " + baseNamespace, result);
        assertEquals(baseNamespace, namespaceService.getCurrentNamespace());
    }

    @Test
    void testExecuteWithTenant_withEmptyTenant() {
        // Given
        String tenantId = "";
        Function<String, String> operation = namespace -> "Result for " + namespace;

        // When
        String result = namespaceService.executeWithTenant(tenantId, operation);

        // Then
        assertEquals("Result for " + baseNamespace, result);
        assertEquals(baseNamespace, namespaceService.getCurrentNamespace());
    }

    @Test
    void testClearTenantNamespace_withExistingKeys() {
        // Given
        String tenantId = "tenant123";
        String expectedPattern = baseNamespace + ":" + tenantId + ":*";
        Set<String> mockKeys = Set.of(
            baseNamespace + ":" + tenantId + ":key1",
            baseNamespace + ":" + tenantId + ":key2"
        );
        
        when(redisTemplate.keys(expectedPattern)).thenReturn(mockKeys);

        // When
        namespaceService.clearTenantNamespace(tenantId);

        // Then
        verify(redisTemplate).keys(expectedPattern);
        verify(redisTemplate).delete(mockKeys);
    }

    @Test
    void testClearTenantNamespace_withNoKeys() {
        // Given
        String tenantId = "tenant123";
        String expectedPattern = baseNamespace + ":" + tenantId + ":*";
        Set<String> emptyKeys = Set.of();
        
        when(redisTemplate.keys(expectedPattern)).thenReturn(emptyKeys);

        // When
        namespaceService.clearTenantNamespace(tenantId);

        // Then
        verify(redisTemplate).keys(expectedPattern);
        verify(redisTemplate, never()).delete((String) any());
    }

    @Test
    void testClearTenantNamespace_withNullKeys() {
        // Given
        String tenantId = "tenant123";
        String expectedPattern = baseNamespace + ":" + tenantId + ":*";
        
        when(redisTemplate.keys(expectedPattern)).thenReturn(null);

        // When
        namespaceService.clearTenantNamespace(tenantId);

        // Then
        verify(redisTemplate).keys(expectedPattern);
        verify(redisTemplate, never()).delete((String) any());
    }

    @Test
    void testThreadLocalIsolation() throws InterruptedException {
        // Given
        String tenant1 = "tenant1";
        String tenant2 = "tenant2";
        String[] results = new String[2];
        Exception[] exceptions = new Exception[2];

        // When - Execute in parallel threads
        Thread thread1 = new Thread(() -> {
            try {
                namespaceService.setTenantNamespace(tenant1);
                Thread.sleep(100); // Simulate some work
                results[0] = namespaceService.getCurrentNamespace();
            } catch (Exception e) {
                exceptions[0] = e;
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                namespaceService.setTenantNamespace(tenant2);
                Thread.sleep(100); // Simulate some work
                results[1] = namespaceService.getCurrentNamespace();
            } catch (Exception e) {
                exceptions[1] = e;
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Then
        assertNull(exceptions[0], "Thread 1 should not have exceptions");
        assertNull(exceptions[1], "Thread 2 should not have exceptions");
        assertEquals(baseNamespace + ":" + tenant1, results[0]);
        assertEquals(baseNamespace + ":" + tenant2, results[1]);
    }

    @Test
    void testConstructorWithNullBaseNamespace() {
        // Given
        String nullBaseNamespace = null;

        // When
        NamespaceService serviceWithNullBase = new NamespaceService(redisTemplate, nullBaseNamespace);

        // Then
        // Should not throw exception, but getCurrentNamespace should handle null
        String currentNamespace = serviceWithNullBase.getCurrentNamespace();
        assertEquals(nullBaseNamespace, currentNamespace);
    }

    @Test
    void testConstructorWithEmptyBaseNamespace() {
        // Given
        String emptyBaseNamespace = "";

        // When
        NamespaceService serviceWithEmptyBase = new NamespaceService(redisTemplate, emptyBaseNamespace);

        // Then
        String currentNamespace = serviceWithEmptyBase.getCurrentNamespace();
        assertEquals(emptyBaseNamespace, currentNamespace);
    }

    @Test
    void testSetTenantNamespace_multipleCalls() {
        // Given
        String tenant1 = "tenant1";
        String tenant2 = "tenant2";

        // When
        namespaceService.setTenantNamespace(tenant1);
        assertEquals(baseNamespace + ":" + tenant1, namespaceService.getCurrentNamespace());

        namespaceService.setTenantNamespace(tenant2);

        // Then
        assertEquals(baseNamespace + ":" + tenant2, namespaceService.getCurrentNamespace());
    }

    @Test
    void testExecuteWithTenant_nestedExecution() {
        // Given
        String tenant1 = "tenant1";
        String tenant2 = "tenant2";
        
        Function<String, String> outerOperation = namespace1 -> {
            String result1 = "Outer: " + namespace1;
            
            // Nested execution
            String result2 = namespaceService.executeWithTenant(tenant2, namespace2 -> {
                return "Inner: " + namespace2;
            });
            
            return result1 + " | " + result2;
        };

        // When
        String result = namespaceService.executeWithTenant(tenant1, outerOperation);

        // Then
        String expected = "Outer: " + baseNamespace + ":" + tenant1 + 
                         " | Inner: " + baseNamespace + ":" + tenant2;
        assertEquals(expected, result);
        
        // Verify original state is restored
        assertEquals(baseNamespace, namespaceService.getCurrentNamespace());
    }
}