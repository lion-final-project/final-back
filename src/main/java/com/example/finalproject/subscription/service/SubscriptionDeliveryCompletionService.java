package com.example.finalproject.subscription.service;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionHistory;
import com.example.finalproject.subscription.repository.SubscriptionHistoryRepository;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배송 완료 시 구독 이력을 반영하는 서비스.
 * StoreOrder가 DELIVERED로 변경될 때 호출하여 연결된 SubscriptionHistory를 COMPLETED로 전환하고,
 * 해당 구독의 cycleCount를 증가시킨다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionDeliveryCompletionService {

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * StoreOrder 배송 완료 시 연결된 구독 이력을 COMPLETED로 반영한다.
     * 구독 주문이 아닌 경우(연결된 SubscriptionHistory가 없는 경우) 아무 작업도 하지 않는다.
     *
     * @param storeOrderId 배송 완료된 StoreOrder ID
     */
    @Transactional
    public void handleDeliveryCompletion(Long storeOrderId) {
        subscriptionHistoryRepository.findFirstByStoreOrderId(storeOrderId)
                .ifPresent(this::completeSubscriptionDelivery);
    }

    private void completeSubscriptionDelivery(SubscriptionHistory history) {
        history.markCompleted();
        subscriptionHistoryRepository.save(history);

        Subscription subscription = history.getSubscription();
        subscription.incrementCycleCount();
        subscriptionRepository.save(subscription);

        log.info("구독 배송 완료 반영: subscriptionHistoryId={}, subscriptionId={}, cycleCount={}",
                history.getId(), subscription.getId(), subscription.getCycleCount());
    }
}
