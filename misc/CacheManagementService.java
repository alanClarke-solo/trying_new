package com.example.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.example.inventory.event.CacheInvalidationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic cacheInvalidationTopic;
    private final ObjectMapper objectMapper;
    private final String redisNamespace;

    @Autowired
    public CacheService(
            CacheManager cacheManager, 
            RedisTemplate<String, Object> redisTemplate,
            ChannelTopic cacheInvalidationTopic,
            ObjectMapper objectMapper,
            @Value("${spring.redis.namespace:inventory}") String redisNamespace) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.cacheInvalidationTopic = cacheInvalidationTopic;
        this.objectMapper = objectMapper;
        this.redisNamespace = redisNamespace;
    }

    /**
     * Invalidates a specific cache entry
     * 
     * @param cacheName the name of the cache
     * @param key the key to invalidate
     */
    public void invalidateCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            log.debug("Invalidating cache entry: {}:{}", cacheName, key);
            cache.evict(key);
        }
    }

    /**
     * Invalidates an entire cache
     * 
     * @param cacheName the name of the cache to invalidate
     */
    public void invalidateEntireCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            log.debug("Invalidating entire cache: {}", cacheName);
            cache.clear();
        }
    }

    /**
     * Publishes a cache invalidation event to Redis pub/sub
     * 
     * @param cacheName the name of the cache
     * @param key the key to invalidate
     */
    public void publishCacheInvalidation(String cacheName, Object key) {
        CacheInvalidationEvent event = new CacheInvalidationEvent(cacheName, String.valueOf(key));
        try {
            String message = objectMapper.writeValueAsString(event);
            log.debug("Publishing cache invalidation event: {}", message);
            redisTemplate.convertAndSend(cacheInvalidationTopic.getTopic(), message);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cache invalidation event", e);
        }
    }
    
    /**
     * Store a value directly in Redis with namespace and TTL
     */
    public void storeValue(String key, Object value, long ttlSeconds) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * Retrieve a value directly from Redis with namespace
     */
    public Object getValue(String key) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }
    
    /**
     * Clear all keys in the current namespace
     */
    public void clearNamespace() {
        Set<String> keys = redisTemplate.keys(redisNamespace + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared all keys in namespace: {}", redisNamespace);
        }
    }
}