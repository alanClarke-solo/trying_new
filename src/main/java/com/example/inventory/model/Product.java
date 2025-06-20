package com.example.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.inventory.model.tracking.TrackChanges;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("PRODUCTS")
@TrackChanges  // Mark the whole class for change tracking
public class Product {

    @Id
    private Long id;

    @Column("NAME")
    private String name;

    @Column("DESCRIPTION")
    private String description;

    @Column("SKU")
    private String sku;

    @Column("PRICE")
    private BigDecimal price;

    @Column("STOCK_QUANTITY")
    private Integer stockQuantity;

    @Column("CATEGORY_ID")
    private Long categoryId;

    @Column("SUPPLIER_ID")
    private Long supplierId;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;

    // These fields are not persisted but used for cache enrichment
    @Transient
    private Category category;

    @Transient
    private Supplier supplier;

    // This field is not persisted but used for version tracking in cache
    @Transient
    private long version;
}