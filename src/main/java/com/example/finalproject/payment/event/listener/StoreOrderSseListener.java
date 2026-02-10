package com.example.finalproject.payment.event.listener;

import com.example.finalproject.global.sse.Service.SseService;
import com.example.finalproject.global.sse.enums.SseEventType;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.event.StoreOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StoreOrderSseListener {

    private final SseService sseService;
    private final StoreOrderRepository storeOrderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StoreOrderCreatedEvent event) {

        StoreOrder storeOrder =
                storeOrderRepository.findById(event.getStoreOrderId())
                        .orElseThrow();

        Long ownerId = storeOrder
                .getStore()
                .getOwner()
                .getId();

        sseService.send(
                ownerId,
                SseEventType.STORE_ORDER_CREATED,
                storeOrder.getId());
    }
}
