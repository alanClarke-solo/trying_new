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
import com.example.inventory.model.Product;
import com.example.inventory.model.Supplier;
import com.example.inventory.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final CacheService cacheService;

    @Autowired
    public ProductService(
            ProductRepository productRepository,
            CategoryService categoryService,
            SupplierService supplierService,
            CacheService cacheService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.supplierService = supplierService;
        this.cacheService = cacheService;
    }

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public Optional<Product> findById(Long id) {
        log.debug("Fetching product from database with ID: {}", id);
        Optional<Product> productOpt = productRepository.findById(id);

        // Enrich with category and supplier data if product exists
        if (productOpt.isPresent()) {
            enrichProduct(productOpt.get());
        }

        return productOpt;
    }

    @Cacheable(value = "products", key = "'sku:' + #sku", unless = "#result == null")
    public Optional<Product> findBySku(String sku) {
        log.debug("Fetching product from database by SKU: {}", sku);
        Optional<Product> productOpt = productRepository.findBySku(sku);

        if (productOpt.isPresent()) {
            enrichProduct(productOpt.get());
        }

        return productOpt;
    }

    @Cacheable(value = "products", key = "'category:' + #categoryId")
    public List<Product> findByCategoryId(Long categoryId) {
        log.debug("Fetching products from database by category ID: {}", categoryId);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        products.forEach(this::enrichProduct);
        return products;
    }

    @Cacheable(value = "products", key = "'supplier:' + #supplierId")
    public List<Product> findBySupplierId(Long supplierId) {
        log.debug("Fetching products from database by supplier ID: {}", supplierId);
        List<Product> products = productRepository.findBySupplierId(supplierId);
        products.forEach(this::enrichProduct);
        return products;
    }

    @Cacheable(value = "products", key = "'all'")
    public List<Product> findAll() {
        log.debug("Fetching all products from database");
        List<Product> products = productRepository.findAllSorted();
        products.forEach(this::enrichProduct);
        return products;
    }

    @Cacheable(value = "products", key = "'lowStock:' + #threshold")
    public List<Product> findLowStockProducts(int threshold) {
        log.debug("Fetching products with stock below: {}", threshold);
        List<Product> products = productRepository.findByStockLessThan(threshold);
        products.forEach(this::enrichProduct);
        return products;
    }

    @Cacheable(value = "products", key = "'search:' + #keyword")
    public List<Product> searchByKeyword(String keyword) {
        log.debug("Searching products with keyword: {}", keyword);
        List<Product> products = productRepository.searchByKeyword(keyword);
        products.forEach(this::enrichProduct);
        return products;
    }

    @Cacheable(value = "products", key = "'byCategory:' + #categoryName")
    public List<Product> findByCategory(String categoryName) {
        log.debug("Fetching products by category name: {}", categoryName);
        List<Product> products = productRepository.findByCategory(categoryName);
        products.forEach(this::enrichProduct);
        return products;
    }

    @Transactional
    @Caching(
            put = {@CachePut(value = "products", key = "#result.id")},
            evict = {
                    @CacheEvict(value = "products", key = "'all'"),
                    @CacheEvict(value = "products", key = "'sku:' + #product.sku", condition = "#product.sku != null"),
                    @CacheEvict(value = "products", key = "'category:' + #product.categoryId", condition = "#product.categoryId != null"),
                    @CacheEvict(value = "products", key = "'supplier:' + #product.supplierId", condition = "#product.supplierId != null")
            }
    )
    public Product save(Product product) {
        log.debug("Saving product: {}", product);
        Product savedProduct = productRepository.save(product);

        // Publish cache invalidation
        cacheService.publishCacheInvalidation("products", "all");
        if (product.getSku() != null) {
            cacheService.publishCacheInvalidation("products", "sku:" + product.getSku());
        }
        if (product.getCategoryId() != null) {
            cacheService.publishCacheInvalidation("products", "category:" + product.getCategoryId());

            // Also invalidate category name based caches if we have the name
            if (product.getCategory() != null && product.getCategory().getName() != null) {
                cacheService.publishCacheInvalidation("products", "byCategory:" + product.getCategory().getName());
            }
        }
        if (product.getSupplierId() != null) {
            cacheService.publishCacheInvalidation("products", "supplier:" + product.getSupplierId());
        }

        // Invalidate low stock caches as this might change stock levels
        cacheService.publishCacheInvalidation("products", "lowStock:10"); // Common threshold

        return savedProduct;
    }

    @Transactional
    @CachePut(value = "products", key = "#id")
    public Product updateStock(Long id, int quantity) {
        log.debug("Updating stock for product ID: {} to {}", id, quantity);
        productRepository.updateStock(id, quantity);

        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product updatedProduct = product.get();
            enrichProduct(updatedProduct);

            // Publish cache invalidation for various stock-related caches
            cacheService.publishCacheInvalidation("products", id);
            cacheService.publishCacheInvalidation("products", "all");
            cacheService.publishCacheInvalidation("products", "lowStock:10"); // Common threshold

            return updatedProduct;
        }

        throw new RuntimeException("Product not found with ID: " + id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", key = "'all'"),
            @CacheEvict(value = "products", key = "'sku:' + #sku", condition = "#sku != null"),
            @CacheEvict(value = "products", key = "'category:' + #categoryId", condition = "#categoryId != null"),
            @CacheEvict(value = "products", key = "'supplier:' + #supplierId", condition = "#supplierId != null")
    })
    public void delete(Long id, String sku, Long categoryId, Long supplierId) {
        log.debug("Deleting product with ID: {}", id);
        productRepository.deleteById(id);

        // Publish cache invalidation
        cacheService.publishCacheInvalidation("products", id);
        cacheService.publishCacheInvalidation("products", "all");
        if (sku != null) {
            cacheService.publishCacheInvalidation("products", "sku:" + sku);
        }
        if (categoryId != null) {
            cacheService.publishCacheInvalidation("products", "category:" + categoryId);
        }
        if (supplierId != null) {
            cacheService.publishCacheInvalidation("products", "supplier:" + supplierId);
        }
        cacheService.publishCacheInvalidation("products", "lowStock:10"); // Common threshold
    }

    // Helper method to enrich a product with its category and supplier
    private void enrichProduct(Product product) {
        if (product.getCategoryId() != null) {
            categoryService.findById(product.getCategoryId())
                    .ifPresent(product::setCategory);
        }

        if (product.getSupplierId() != null) {
            supplierService.findById(product.getSupplierId())
                    .ifPresent(product::setSupplier);
        }
    }
}
