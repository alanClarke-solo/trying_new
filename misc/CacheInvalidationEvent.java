package com.example.inventory.event;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheInvalidationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String cacheName;
    private String key;
}