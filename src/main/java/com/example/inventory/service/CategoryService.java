package com.example.inventory.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory.model.Category;
import com.example.inventory.repository.CategoryRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CacheService cacheService;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, CacheService cacheService) {
        this.categoryRepository = categoryRepository;
        this.cacheService = cacheService;
    }

    @Cacheable(value = "categories", key = "#id", unless = "#result == null")
    public Optional<Category> findById(Long id) {
        log.debug("Fetching category from database with ID: {}", id);
        return categoryRepository.findById(id);
    }

    @Cacheable(value = "categories", key = "'name:' + #name", unless = "#result == null")
    public Optional<Category> findByName(String name) {
        log.debug("Fetching category from database by name: {}", name);
        return categoryRepository.findByName(name);
    }

    @Cacheable(value = "categories", key = "'all'")
    public List<Category> findAll() {
        log.debug("Fetching all categories from database");
        return categoryRepository.findAllSorted();
    }

    @Cacheable(value = "categories", key = "'search:' + #keyword")
    public List<Category> searchByKeyword(String keyword) {
        log.debug("Searching categories with keyword: {}", keyword);
        return categoryRepository.searchByKeyword(keyword);
    }

    @Transactional
    @Caching(
            put = {@CachePut(value = "categories", key = "#result.id")},
            evict = {
                    @CacheEvict(value = "categories", key = "'all'"),
                    @CacheEvict(value = "categories", key = "'name:' + #category.name", condition = "#category.name != null")
            }
    )
    public Category save(Category category) {
        log.debug("Saving category: {}", category);
        Category savedCategory = categoryRepository.save(category);

        // Publish cache invalidation to keep other instances up-to-date
        cacheService.publishCacheInvalidation("categories", "all");
        if (category.getName() != null) {
            cacheService.publishCacheInvalidation("categories", "name:" + category.getName());
        }

        return savedCategory;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", key = "#id"),
            @CacheEvict(value = "categories", key = "'all'"),
            @CacheEvict(value = "categories", key = "'name:' + #name", condition = "#name != null")
    })
    public void delete(Long id, String name) {
        log.debug("Deleting category with ID: {}", id);
        categoryRepository.deleteById(id);

        // Publish cache invalidation
        cacheService.publishCacheInvalidation("categories", id);
        cacheService.publishCacheInvalidation("categories", "all");
        if (name != null) {
            cacheService.publishCacheInvalidation("categories", "name:" + name);
        }
    }
}
