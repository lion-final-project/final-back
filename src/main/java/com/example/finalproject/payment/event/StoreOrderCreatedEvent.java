package com.example.finalproject.payment.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreOrderCreatedEvent {
    private final Long storeOrderId;
    private final LocalDateTime orderedAt;
}
