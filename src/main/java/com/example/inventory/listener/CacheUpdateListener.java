package com.example.inventory.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.inventory.event.CacheInvalidationEvent;
import com.example.inventory.service.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheUpdateListener {

    private final CacheService cacheService;

    /**
     * Handle cache invalidation events after a transaction is completed.
     * This ensures that the cache is only invalidated if the database transaction succeeds.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheInvalidation(CacheInvalidationEvent event) {
        log.debug("Handling cache invalidation event after transaction: {}", event);
        cacheService.invalidateCache(event.getCacheName(), event.getKey());
    }
}