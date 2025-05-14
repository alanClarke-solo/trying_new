package com.example.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.Function;

@Service
@Slf4j
public class NamespaceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String baseNamespace;
    private final ThreadLocal<String> tenantNamespace = new ThreadLocal<>();

    @Autowired
    public NamespaceService(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${spring.redis.namespace:inventory}") String baseNamespace) {
        this.redisTemplate = redisTemplate;
        this.baseNamespace = baseNamespace;
    }

    /**
     * Set the current tenant namespace for the thread
     */
    public void setTenantNamespace(String tenantId) {
        tenantNamespace.set(tenantId);
        log.debug("Set tenant namespace to: {}", tenantId);
    }

    /**
     * Clear the tenant namespace from the thread
     */
    public void clearTenantNamespace() {
        tenantNamespace.remove();
    }

    /**
     * Get the current namespace (base + tenant if available)
     */
    public String getCurrentNamespace() {
        String tenant = tenantNamespace.get();
        if (tenant != null && !tenant.isEmpty()) {
            return baseNamespace + ":" + tenant;
        }
        return baseNamespace;
    }

    /**
     * Execute a Redis operation within a specific tenant namespace
     */
    public <T> T executeWithTenant(String tenantId, Function<String, T> operation) {
        String previousTenant = tenantNamespace.get();
        try {
            setTenantNamespace(tenantId);
            return operation.apply(getCurrentNamespace());
        } finally {
            if (previousTenant != null) {
                tenantNamespace.set(previousTenant);
            } else {
                tenantNamespace.remove();
            }
        }
    }

    /**
     * Clear all keys in a specific tenant namespace
     */
    public void clearTenantNamespace(String tenantId) {
        String namespace = baseNamespace + ":" + tenantId + ":*";
        Set<String> keys = redisTemplate.keys(namespace);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared all keys in tenant namespace: {}", namespace);
        }
    }
}