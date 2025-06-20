package com.example.inventory.aop;

import com.example.inventory.model.tracking.FieldChangeTrackingStore;
import com.example.inventory.model.tracking.TrackChanges;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class FieldChangeTrackingAspect {

    private final FieldChangeTrackingStore trackingStore;

    // Cache to store field mappings for classes to avoid repeated reflection
    private final Map<Class<?>, Map<String, String>> classFieldMappings = new ConcurrentHashMap<>();

    @Autowired
    public FieldChangeTrackingAspect(FieldChangeTrackingStore trackingStore) {
        this.trackingStore = trackingStore;
    }

    /**
     * Intercept setter method calls on classes annotated with @TrackChanges
     */
    @Before("execution(* set*(..)) && " +
            "(@target(com.example.inventory.model.tracking.TrackChanges) || " +
            "@annotation(com.example.inventory.model.tracking.TrackChanges)) && " +
            "args(newValue)")
    public void trackFieldChange(JoinPoint joinPoint, Object newValue) {
        try {
            Object entity = joinPoint.getTarget();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();

            // Only process setter methods (starting with "set")
            if (!methodName.startsWith("set") || methodName.length() <= 3) {
                return;
            }

            // Convert the setter method to field name (e.g., "setName" -> "name")
            String fieldName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);

            // Check if the field is annotated or the class is annotated
            if (!isTrackableField(entity.getClass(), fieldName)) {
                return;
            }

            // Get the current field value
            Object currentValue = getCurrentFieldValue(entity, fieldName);

            // Check if the field value has changed
            if (hasValueChanged(currentValue, newValue)) {
                trackingStore.markUpdated(entity, fieldName);
                log.debug("Field '{}' marked as updated in {}", fieldName, entity.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Error in field change tracking: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if a field should be tracked (either field or class is annotated)
     */
    private boolean isTrackableField(Class<?> clazz, String fieldName) {
        try {
            // Check if the class is annotated with @TrackChanges
            if (clazz.isAnnotationPresent(TrackChanges.class)) {
                return true;
            }

            // Check if the field is annotated with @TrackChanges
            Field field = findField(clazz, fieldName);
            return field != null && field.isAnnotationPresent(TrackChanges.class);
        } catch (Exception e) {
            log.warn("Error checking if field is trackable: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the current value of a field using reflection
     */
    private Object getCurrentFieldValue(Object target, String fieldName) {
        try {
            // Get mapping of bean property names to actual field names
            Map<String, String> fieldMappings = getFieldMappings(target.getClass());

            // Get the actual field name (may differ from the property name)
            String actualFieldName = fieldMappings.getOrDefault(fieldName, fieldName);

            // Get the field and make it accessible
            Field field = findField(target.getClass(), actualFieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(target);
            }
        } catch (Exception e) {
            log.warn("Could not get current value for field '{}': {}", fieldName, e.getMessage());
        }
        return null;
    }

    /**
     * Find a field in the class hierarchy
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Get or build a mapping of bean property names to actual field names for a class
     */
    private Map<String, String> getFieldMappings(Class<?> clazz) {
        return classFieldMappings.computeIfAbsent(clazz, c -> {
            Map<String, String> mappings = new HashMap<>();
            // For complex mappings (like in case of custom naming strategies)
            // you might need more sophisticated logic here
            return mappings;
        });
    }

    /**
     * Check if a value has changed
     */
    private boolean hasValueChanged(Object oldValue, Object newValue) {
        // Handle null cases
        if (oldValue == null) {
            return newValue != null;
        }

        // Use equals for value comparison
        return !oldValue.equals(newValue);
    }
}