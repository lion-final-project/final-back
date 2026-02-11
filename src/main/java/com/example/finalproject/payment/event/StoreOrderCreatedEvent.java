package com.example.finalproject.payment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreOrderCreatedEvent {
    private final Long storeOrderId;
}
