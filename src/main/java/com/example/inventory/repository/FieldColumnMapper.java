package com.example.inventory.repository;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FieldColumnMapper {

    // Cache mapping to avoid repeated reflection
    private final Map<String, String> fieldToColumnCache = new ConcurrentHashMap<>();

    /**
     * Gets the database column name for a given field
     * 
     * @param entityClass the entity class
     * @param fieldName the field name
     * @return the column name or null if not found
     */
    public String getColumnNameForField(Class<?> entityClass, String fieldName) {
        String cacheKey = entityClass.getName() + "." + fieldName;
        
        return fieldToColumnCache.computeIfAbsent(cacheKey, key -> {
            try {
                Field field = findField(entityClass, fieldName);
                if (field == null) {
                    return null;
                }
                
                Column column = field.getAnnotation(Column.class);
                if (column != null && !column.value().isEmpty()) {
                    return column.value();
                }
                
                // Default convention: convert camelCase to UPPER_SNAKE_CASE
                return camelToUpperSnakeCase(fieldName);
            } catch (Exception e) {
                return null;
            }
        });
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
     * Convert camelCase to UPPER_SNAKE_CASE
     */
    private String camelToUpperSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
}