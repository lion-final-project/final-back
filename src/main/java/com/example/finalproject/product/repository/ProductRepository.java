package com.example.finalproject.product.repository;

import com.example.finalproject.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * ID와 마트 ID로 상품을 조회한다. (해당 마트 소속 상품인지 검증용)
     *
     * @param id      상품 ID
     * @param storeId 마트 ID
     * @return 상품 (존재하고 해당 마트 소속일 때만)
     */
    Optional<Product> findByIdAndStore_Id(Long id, Long storeId);
}