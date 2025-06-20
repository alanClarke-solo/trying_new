package com.example.inventory.repository;

import com.example.inventory.model.Product;
import com.example.inventory.model.tracking.FieldChangeTrackingStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.StringJoiner;

@Repository
public class ProductCustomRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final FieldColumnMapper fieldColumnMapper;
    private final FieldChangeTrackingStore trackingStore;

    @Autowired
    public ProductCustomRepository(
            NamedParameterJdbcTemplate jdbcTemplate,
            FieldColumnMapper fieldColumnMapper,
            FieldChangeTrackingStore trackingStore) {
        this.jdbcTemplate = jdbcTemplate;
        this.fieldColumnMapper = fieldColumnMapper;
        this.trackingStore = trackingStore;
    }

    /**
     * Updates only the fields that have been modified in the product
     */
    public void updateSelectively(Product product) {
        // Get updated fields from the tracking store
        Set<String> updatedFields = trackingStore.getUpdatedFields(product);

        if (updatedFields.isEmpty()) {
            return; // No fields to update
        }

        StringJoiner setClause = new StringJoiner(", ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", product.getId());

        // Always update the updated_at timestamp
        setClause.add("UPDATED_AT = :updatedAt");
        params.addValue("updatedAt", LocalDateTime.now());

        // Add each modified field to the query
        updatedFields.forEach(fieldName -> {
            String columnName = fieldColumnMapper.getColumnNameForField(Product.class, fieldName);
            if (columnName != null) {
                setClause.add(columnName + " = :" + fieldName);
                params.addValue(fieldName, getFieldValue(product, fieldName));
            }
        });

        String sql = "UPDATE PRODUCTS SET " + setClause + " WHERE ID = :id";

        jdbcTemplate.update(sql, params);

        // Clear the updated fields after successful update
        trackingStore.clearUpdatedFields(product);
    }

    /**
     * Gets a field value from an object using reflection
     */
    private Object getFieldValue(Object object, String fieldName) {
        try {
            java.lang.reflect.Field field = findField(object.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(object);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not get field value for " + fieldName, e);
        }
        return null;
    }

    /**
     * Find a field in the class hierarchy
     */
    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}