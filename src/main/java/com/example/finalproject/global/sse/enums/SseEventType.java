package com.example.finalproject.global.sse.enums;

public enum SseEventType {
    UNREAD_COUNT("unread-count"),
    CONNECTED("connected"),
    ORDER_CREATED("order-created");


    private final String eventName;

    SseEventType(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}