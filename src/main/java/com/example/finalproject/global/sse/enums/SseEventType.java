package com.example.finalproject.global.sse.enums;

public enum SseEventType {
    UNREAD_COUNT("unread-count"),
    CONNECTED("connected"),
    STORE_ORDER_CREATED("store-order-created"),
    NEW_DELIVERY("new-delivery"),
    NEARBY_DELIVERIES("nearby-deliveries"),
    DELIVERY_MATCHED("delivery-matched");

    private final String eventName;

    SseEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}