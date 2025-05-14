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

import com.example.inventory.model.Supplier;
import com.example.inventory.repository.SupplierRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final CacheService cacheService;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository, CacheService cacheService) {
        this.supplierRepository = supplierRepository;
        this.cacheService = cacheService;
    }

    @Cacheable(value = "suppliers", key = "#id", unless = "#result == null")
    public Optional<Supplier> findById(Long id) {
        log.debug("Fetching supplier from database with ID: {}", id);
        return supplierRepository.findById(id);
    }

    @Cacheable(value = "suppliers", key = "'name:' + #name", unless = "#result == null")
    public Optional<Supplier> findByName(String name) {
        log.debug("Fetching supplier from database by name: {}", name);
        return supplierRepository.findByName(name);
    }

    @Cacheable(value = "suppliers", key = "'email:' + #email", unless = "#result == null")
    public Optional<Supplier> findByEmail(String email) {
        log.debug("Fetching supplier from database by email: {}", email);
        return supplierRepository.findByEmail(email);
    }

    @Cacheable(value = "suppliers", key = "'all'")
    public List<Supplier> findAll() {
        log.debug("Fetching all suppliers from database");
        return supplierRepository.findAllSorted();
    }

    @Cacheable(value = "suppliers", key = "'search:' + #keyword")
    public List<Supplier> searchByKeyword(String keyword) {
        log.debug("Searching suppliers with keyword: {}", keyword);
        return supplierRepository.searchByKeyword(keyword);
    }

    @Transactional
    @Caching(
            put = {@CachePut(value = "suppliers", key = "#result.id")},
            evict = {
                    @CacheEvict(value = "suppliers", key = "'all'"),
                    @CacheEvict(value = "suppliers", key = "'name:' + #supplier.name", condition = "#supplier.name != null"),
                    @CacheEvict(value = "suppliers", key = "'email:' + #supplier.email", condition = "#supplier.email != null")
            }
    )
    public Supplier save(Supplier supplier) {
        log.debug("Saving supplier: {}", supplier);
        Supplier savedSupplier = supplierRepository.save(supplier);

        // Publish cache invalidation
        cacheService.publishCacheInvalidation("suppliers", "all");
        if (supplier.getName() != null) {
            cacheService.publishCacheInvalidation("suppliers", "name:" + supplier.getName());
        }
        if (supplier.getEmail() != null) {
            cacheService.publishCacheInvalidation("suppliers", "email:" + supplier.getEmail());
        }

        return savedSupplier;
    }

        @Transactional
        @Caching(evict = {
                @CacheEvict(value = "suppliers", key = "#id"),
                @CacheEvict(value = "suppliers", key = "'all'"),
                @CacheEvict(value = "suppliers", key = "'name:' + #name", condition = "#name != null"),
                @CacheEvict(value = "suppliers", key = "'email:' + #email", condition = "#email != null")
        })
        public void delete(Long id, String name, String email) {
            log.debug("Deleting supplier with ID: {}", id);
            supplierRepository.deleteById(id);

            // Publish cache invalidation
            cacheService.publishCacheInvalidation("suppliers", id);
            cacheService.publishCacheInvalidation("suppliers", "all");
            if (name != null) {
                cacheService.publishCacheInvalidation("suppliers", "name:" + name);
            }
            if (email != null) {
                cacheService.publishCacheInvalidation("suppliers", "email:" + email);
            }
        }
    }
