package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.domain.SubscriptionProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionProductItemRepository extends JpaRepository<SubscriptionProductItem, Long> {

    /**
     * 구독 상품에 속한 구성 품목 목록을 ID 순으로 조회한다.
     *
     * @param subscriptionProduct 구독 상품
     * @return 구성 품목 목록
     */
    List<SubscriptionProductItem> findBySubscriptionProductOrderById(SubscriptionProduct subscriptionProduct);
}
