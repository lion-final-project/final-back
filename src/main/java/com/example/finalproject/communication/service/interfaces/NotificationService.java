package com.example.finalproject.communication.service.interfaces;

public interface NotificationService {

    void createOrderPaidNotification(Long userId, Long orderId, String orderNumber, Integer amount);
}
