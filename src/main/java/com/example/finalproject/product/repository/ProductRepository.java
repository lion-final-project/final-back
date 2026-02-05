package com.example.finalproject.product.repository;

import com.example.finalproject.product.domain.Product;
import com.example.finalproject.store.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    Optional<Product> findByIdAndDeletedAtIsNull(Long id);

    Page<Product> findByStoreAndDeletedAtIsNull(Store store, Pageable pageable);

    Long countByStoreAndDeletedAtIsNull(Store store);

    Long countByStoreAndIsActiveAndDeletedAtIsNull(Store store, Boolean isActive);

    boolean existsByStoreAndProductNameAndDeletedAtIsNull(Store store, String productName);
}