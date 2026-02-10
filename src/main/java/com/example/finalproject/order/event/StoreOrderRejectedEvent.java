package com.example.finalproject.order.event;

import lombok.Getter;

@Getter
public class StoreOrderRejectedEvent {

    private final Long customerId;
    private final String storeName;

    public StoreOrderRejectedEvent(Long customerId, String storeName) {
        this.customerId = customerId;
        this.storeName = storeName;
    }
}
