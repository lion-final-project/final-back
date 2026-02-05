package com.example.finalproject.product.repository;

import com.example.finalproject.product.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Collection<Object> findByCategoryName(String categoryName);
}
