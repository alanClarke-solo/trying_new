package com.example.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.inventory.model.Category;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {

    Optional<Category> findByName(String name);

    @Query("SELECT c.* FROM categories c ORDER BY c.name")
    List<Category> findAllSorted();

    @Query("SELECT c.* FROM categories c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Category> searchByKeyword(@Param("keyword") String keyword);
}