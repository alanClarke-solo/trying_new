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
    private final ChannelTopic cacheUpdatesTopic;
    private final ObjectMapper objectMapper;
    private final String redisNamespace;

    @Autowired
    public CacheService(
            CacheManager cacheManager,
            RedisTemplate<String, Object> redisTemplate,
            ChannelTopic cacheUpdatesTopic,
            ObjectMapper objectMapper,
            @Value("${spring.redis.namespace:inventory}") String redisNamespace) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
        this.cacheUpdatesTopic = cacheUpdatesTopic;
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
        CacheInvalidationEvent event = new CacheInvalidationEvent(cacheName, key.toString());
        try {
            log.debug("Publishing cache invalidation event: {}", event);
            redisTemplate.convertAndSend(cacheUpdatesTopic.getTopic(), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to publish cache invalidation event", e);
        }
    }

    public void invalidateCache(String cacheName, String key) {
        try {
            log.debug("Evicting from cache: {}:{}", cacheName, key);
            cacheManager.getCache(cacheName).evict(key);
        } catch (Exception e) {
            log.error("Failed to evict cache entry: {}:{}", cacheName, key, e);
        }
    }

    public void clearCache(String cacheName) {
        try {
            log.debug("Clearing cache: {}", cacheName);
            cacheManager.getCache(cacheName).clear();
        } catch (Exception e) {
            log.error("Failed to clear cache: {}", cacheName, e);
        }
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
