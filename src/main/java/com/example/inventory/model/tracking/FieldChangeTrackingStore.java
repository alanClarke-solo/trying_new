package com.example.inventory.model.tracking;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central store for tracking field changes in entities
 */
@Component
public class FieldChangeTrackingStore {
    
    // Store updated fields for each entity instance
    private final Map<Object, Set<String>> entityUpdatedFields = new ConcurrentHashMap<>();
    
    /**
     * Mark a field as updated for an entity
     */
    public void markUpdated(Object entity, String fieldName) {
        entityUpdatedFields.computeIfAbsent(entity, k -> new HashSet<>()).add(fieldName);
    }
    
    /**
     * Get all updated fields for an entity
     */
    public Set<String> getUpdatedFields(Object entity) {
        return Collections.unmodifiableSet(
            entityUpdatedFields.getOrDefault(entity, Collections.emptySet())
        );
    }
    
    /**
     * Check if a specific field has been updated
     */
    public boolean isFieldUpdated(Object entity, String fieldName) {
        return entityUpdatedFields.getOrDefault(entity, Collections.emptySet()).contains(fieldName);
    }
    
    /**
     * Clear all updated fields for an entity
     */
    public void clearUpdatedFields(Object entity) {
        entityUpdatedFields.computeIfAbsent(entity, k -> new HashSet<>()).clear();
    }
    
    /**
     * Clean up tracking data when an entity is no longer needed
     * (can be called by a garbage collection listener or explicitly)
     */
    public void removeEntity(Object entity) {
        entityUpdatedFields.remove(entity);
    }
}