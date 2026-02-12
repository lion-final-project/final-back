package com.example.finalproject.delivery.event;

import com.example.finalproject.delivery.enums.DeliveryStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 배달 상태 변경 이벤트 (모든 상태 변경 시 발행)
 */
@Getter
public class DeliveryStatusChangedEvent extends ApplicationEvent {
    private final Long deliveryId;
    private final DeliveryStatus newStatus;
    private final Long riderId;
    private final Long customerId;

    public DeliveryStatusChangedEvent(Object source, Long deliveryId,
            DeliveryStatus newStatus,
            Long riderId, Long customerId) {
        super(source);
        this.deliveryId = deliveryId;
        this.newStatus = newStatus;
        this.riderId = riderId;
        this.customerId = customerId;
    }
}
