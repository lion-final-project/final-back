package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 구독 상품별로 활성(ACTIVE) 구독자 수를 센다.
     *
     * @param subscriptionProduct 구독 상품
     * @param status              구독 상태 (ACTIVE 등)
     * @return 해당 상태의 구독 건수
     */
    long countBySubscriptionProductAndStatus(SubscriptionProduct subscriptionProduct, SubscriptionStatus status);
}
