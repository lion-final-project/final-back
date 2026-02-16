package com.example.finalproject.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreOrderRefundCompletedEvent {

    private final Long storeOrderId;
    private final int refundAmount;
    private final String reason;
}
