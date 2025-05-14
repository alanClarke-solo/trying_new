package com.example.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.inventory.model.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySupplierId(Long supplierId);

    @Query("SELECT p.* FROM products p ORDER BY p.name")
    List<Product> findAllSorted();

    @Query("SELECT p.* FROM products p WHERE p.stock_quantity < :threshold")
    List<Product> findByStockLessThan(@Param("threshold") int threshold);

    @Query("SELECT p.* FROM products p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    @Modifying
    @Query("UPDATE products SET stock_quantity = :quantity WHERE id = :id")
    void updateStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Query("SELECT p.* FROM products p " +
            "JOIN categories c ON p.category_id = c.id " +
            "JOIN suppliers s ON p.supplier_id = s.id " +
            "WHERE LOWER(c.name) = LOWER(:categoryName)")
    List<Product> findByCategory(@Param("categoryName") String categoryName);
}