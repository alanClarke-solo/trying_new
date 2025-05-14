package com.example.inventory.model;

import java.time.LocalDateTime;

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
@Table("CATEGORIES")
public class Category {

    @Id
    private Long id;

    @Column("NAME")
    private String name;

    @Column("DESCRIPTION")
    private String description;

    @Column("CREATED_AT")
    private LocalDateTime createdAt;

    @Column("UPDATED_AT")
    private LocalDateTime updatedAt;

    // This field is not persisted but used for version tracking in cache
    @Transient
    private long version;
}