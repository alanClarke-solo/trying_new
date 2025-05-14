package com.example.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.inventory.model.Supplier;

@Repository
public interface SupplierRepository extends CrudRepository<Supplier, Long> {

    Optional<Supplier> findByName(String name);

    Optional<Supplier> findByEmail(String email);

    @Query("SELECT s.* FROM suppliers s ORDER BY s.name")
    List<Supplier> findAllSorted();

    @Query("SELECT s.* FROM suppliers s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.contact_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Supplier> searchByKeyword(@Param("keyword") String keyword);
}