package com.example.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.example.inventory.event.CacheInvalidationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheService2 {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic cacheUpdatesTopic;
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    @Autowired
    public CacheService2(
            RedisTemplate<String, Object> redisTemplate,
            ChannelTopic cacheUpdatesTopic,
            CacheManager cacheManager,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.cacheUpdatesTopic = cacheUpdatesTopic;
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
    }

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
}