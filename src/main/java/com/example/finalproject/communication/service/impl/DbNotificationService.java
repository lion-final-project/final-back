package com.example.finalproject.communication.service.impl;

import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.communication.service.OrderPaidNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DbNotificationService implements OrderPaidNotificationService {

    private final NotificationService notificationService;

    @Override
    public void createOrderPaidNotification(Long userId, Long orderId, String orderNumber, Integer amount) {
        // 결제 성공으로 order.status = PAID(또는 완료 상태) 확정 직후 호출
        String title = "주문 결제 완료";
        String content = String.format("주문 %s 결제완료 (orderId=%d, amount=%d)", orderNumber, orderId, amount);
        notificationService.notifyUser(userId, title, content, NotificationRefType.ORDER);
    }
}
