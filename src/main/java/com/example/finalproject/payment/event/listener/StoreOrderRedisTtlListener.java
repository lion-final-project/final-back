package com.example.finalproject.payment.event.listener;

import com.example.finalproject.order.service.StoreOrderTtlService;
import com.example.finalproject.payment.event.StoreOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreOrderRedisTtlListener {

    private final StoreOrderTtlService storeOrderTtlService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StoreOrderCreatedEvent event) {
        log.info("[TTL][추적] StoreOrderCreatedEvent 수신(AFTER_COMMIT) - storeOrderId={}, orderedAt={}", event.getStoreOrderId(), event.getOrderedAt());
        storeOrderTtlService.setAutoReject(event.getStoreOrderId(), event.getOrderedAt());
    }
}
