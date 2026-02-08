package com.example.finalproject.communication.service;

/**
 * 결제 성공으로 주문 상태가 PAID(또는 완료)로 확정될 때 호출하는 알림 연동 포인트.
 * <p>호출 위치: 주문 생성/결제 완료 처리 시 (OrderService 또는 PaymentCallbackService 등)에서
 * order.status = PAID 및 payment 저장 직후.</p>
 * <p>예시:</p>
 * <pre>
 * orderPaidNotificationService.createOrderPaidNotification(
 *     order.getUser().getId(), order.getId(), order.getOrderNumber(), order.getFinalPrice());
 * </pre>
 */
public interface OrderPaidNotificationService {

    void createOrderPaidNotification(Long userId, Long orderId, String orderNumber, Integer amount);
}
