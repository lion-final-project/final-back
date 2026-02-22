package com.example.finalproject.store.repository;

import com.example.finalproject.store.domain.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Long> {
    Optional<StoreCategory> findByCategoryName(String categoryName);
}
