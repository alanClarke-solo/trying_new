package com.example.inventory.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.example.inventory.event.CacheInvalidationEvent;
import com.example.inventory.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisPubSubListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final CacheService cacheService;
    private final String redisNamespace;

    @Autowired
    public RedisPubSubListener(
            ObjectMapper objectMapper,
            CacheService cacheService,
            @Value("${spring.redis.namespace:inventory}") String redisNamespace) {
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
        this.redisNamespace = redisNamespace;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            log.debug("Received Redis message via Lettuce on namespace {}: {}",
                    redisNamespace, messageBody);

            CacheInvalidationEvent event = objectMapper.readValue(messageBody, CacheInvalidationEvent.class);

            // Invalidate the cache entry based on the received event
            log.debug("Invalidating cache: {}:{}", event.getCacheName(), event.getKey());
            cacheService.invalidateCache(event.getCacheName(), event.getKey());

        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }
}
