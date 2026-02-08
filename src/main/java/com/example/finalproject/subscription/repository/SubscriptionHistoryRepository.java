package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionHistory;
import com.example.finalproject.subscription.enums.SubHistoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    /**
     * 구독의 배송 이력 중 특정 상태인 건수를 센다 (UC-C10 남은 배송 건수 등 계산용).
     *
     * @param subscription 구독
     * @param status       이력 상태 (SCHEDULED, ORDERED, COMPLETED, SKIPPED)
     * @return 해당 상태 건수
     */
    long countBySubscriptionAndStatus(Subscription subscription, SubHistoryStatus status);
}
