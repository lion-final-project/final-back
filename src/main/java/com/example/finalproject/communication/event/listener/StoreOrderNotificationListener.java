package com.example.finalproject.communication.event.listener;

import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.order.event.StoreOrderAcceptedEvent;
import com.example.finalproject.order.event.StoreOrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class StoreOrderNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStoreOrderAccepted(StoreOrderAcceptedEvent event) {

        notificationService.createNotification(
                event.getCustomerId(),
                "주문 접수 완료",
                event.getStoreName() + " 매장에서 주문 " + event.getOrderNumber() + " 이(가) 접수되었습니다.",
                NotificationRefType.ORDER
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStoreOrderRejected(StoreOrderRejectedEvent event) {
        notificationService.createNotification(
                event.getCustomerId(),
                "주문 거절",
                event.getStoreName() + " 매장에서 주문이 거절되었습니다.",
                NotificationRefType.ORDER
        );
    }

}
