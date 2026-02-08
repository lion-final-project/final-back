package com.example.finalproject.communication.service;

//주문 결제 완료 시 알림 생성용 인터페이스.
public interface OrderPaidNotificationService {

    void createOrderPaidNotification(Long userId, Long orderId, String orderNumber, Integer amount);
}
