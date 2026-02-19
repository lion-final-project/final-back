package com.example.finalproject.order.listener;

import com.example.finalproject.order.event.StoreOrderRefundCompletedEvent;
import com.example.finalproject.order.service.StoreOrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreOrderRefundListener {

    private final StoreOrderStatusService storeOrderStatusService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StoreOrderRefundCompletedEvent event) {
        storeOrderStatusService.handleRefundCompletion(event.getStoreOrderId(), event.getReason());
    }
}