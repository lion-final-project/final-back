package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.SubscriptionProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionProductRepository extends JpaRepository<SubscriptionProduct, Long> {

    /**
     * 마트 ID로 구독 상품 목록을 생성일 역순으로 조회한다.
     *
     * @param storeId 마트 ID
     * @return 구독 상품 목록
     */
    List<SubscriptionProduct> findByStoreIdOrderByCreatedAtDesc(Long storeId);
}
