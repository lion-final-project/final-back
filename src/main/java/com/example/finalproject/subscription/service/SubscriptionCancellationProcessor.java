package com.example.finalproject.subscription.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionStatusLog;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import com.example.finalproject.subscription.repository.SubscriptionStatusLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 해지 예정(CANCELLATION_PENDING) 구독 단건을 최종 해지(CANCELLED)로 전환한다.
 * 각 건이 독립된 트랜잭션에서 처리되어 일부 실패가 전체에 영향을 주지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionCancellationProcessor {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionStatusLogRepository subscriptionStatusLogRepository;

    @Transactional
    public void processSingleCancellation(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        SubscriptionStatus before = subscription.getStatus();
        subscription.finalizeCancellation();

        subscriptionStatusLogRepository.save(
                SubscriptionStatusLog.builder()
                        .subscription(subscription)
                        .fromStatus(before)
                        .toStatus(SubscriptionStatus.CANCELLED)
                        .build()
        );
        log.info("구독 자동 해지 완료: subscriptionId={}", subscriptionId);
    }
}