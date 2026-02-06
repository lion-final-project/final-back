package com.example.finalproject.communication.service;

/**
 * 결제 성공으로 주문 상태가 PAID(또는 완료)로 확정될 때 호출하는 알림 연동 포인트.
 * 시영님 결제 성공 처리 후 이 인터페이스를 주입받아 호출하면 됨.
 */
public interface OrderPaidNotificationService {

    void createOrderPaidNotification(Long userId, Long orderId, String orderNumber, Integer amount);
}
