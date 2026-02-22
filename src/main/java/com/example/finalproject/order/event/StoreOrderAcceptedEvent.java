package com.example.finalproject.order.event;

import lombok.Getter;

@Getter
public class StoreOrderAcceptedEvent {

    private final Long customerId;
    private final String orderNumber;
    private final String storeName;

    public StoreOrderAcceptedEvent(Long customerId, String orderNumber, String storeName) {
        this.customerId = customerId;
        this.orderNumber = orderNumber;
        this.storeName = storeName;
    }
}
