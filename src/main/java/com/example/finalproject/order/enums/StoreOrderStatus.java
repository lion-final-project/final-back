package com.example.finalproject.order.enums;

public enum StoreOrderStatus {
    PENDING,
    ACCEPTED,
    READY,
    PICKED_UP,
    DELIVERING,
    DELIVERED,

    // 주문 취소
    CANCEL_REQUESTED,
    CANCELLED,
    CANCEL_FAILED,

    REJECT_REQUESTED,
    REJECTED,
}
