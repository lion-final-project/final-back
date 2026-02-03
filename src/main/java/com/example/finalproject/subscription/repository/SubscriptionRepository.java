package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 구독 상품별로 활성(ACTIVE) 구독자 수를 센다.
     *
     * @param subscriptionProduct 구독 상품
     * @param status              구독 상태 (ACTIVE 등)
     * @return 해당 상태의 구독 건수
     */
    long countBySubscriptionProductAndStatus(SubscriptionProduct subscriptionProduct, SubscriptionStatus status);

    /**
     * 구독 상품에 대해 특정 상태 집합에 속하는 구독이 존재하는지 여부를 확인한다.
     *
     * @param subscriptionProduct 구독 상품
     * @param statuses            구독 상태 목록
     * @return true: 존재, false: 없음
     */
    boolean existsBySubscriptionProductAndStatusIn(SubscriptionProduct subscriptionProduct, Collection<SubscriptionStatus> statuses);

    /**
     * 구독 상품에 대해 특정 상태 집합에 속한 구독 건수를 계산한다.
     *
     * @param subscriptionProduct 구독 상품
     * @param statuses            구독 상태 목록
     * @return 해당 상태 집합에 속한 구독 수
     */
    long countBySubscriptionProductAndStatusIn(SubscriptionProduct subscriptionProduct, Collection<SubscriptionStatus> statuses);
}
