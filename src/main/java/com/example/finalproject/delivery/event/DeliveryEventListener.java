package com.example.finalproject.delivery.event;

import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.global.sse.Service.SseService;
import com.example.finalproject.global.sse.enums.SseEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * 배달 상태 변경 이벤트 리스너.
 * 알림 저장 + SSE 실시간 전송을 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventListener {

    private final NotificationService notificationService;
    private final SseService sseService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        log.info("배달 상태 변경 이벤트 수신 - deliveryId: {}, status: {}",
                event.getDeliveryId(), event.getNewStatus());

        // 고객에게 SSE 실시간 알림
        if (event.getCustomerId() != null) {
            Map<String, Object> sseData = Map.of(
                    "deliveryId", event.getDeliveryId(),
                    "status", event.getNewStatus().name());
            sseService.send(event.getCustomerId(), SseEventType.DELIVERY_STATUS_CHANGED, sseData);

            // 완료/취소 시 푸시 알림 저장
            if (event.getNewStatus() == DeliveryStatus.DELIVERED) {
                notificationService.createNotification(
                        event.getCustomerId(),
                        "배달 완료",
                        "주문하신 상품이 배달 완료되었습니다.",
                        NotificationRefType.DELIVERY);
            } else if (event.getNewStatus() == DeliveryStatus.CANCELLED) {
                notificationService.createNotification(
                        event.getCustomerId(),
                        "배달 취소",
                        "배달이 취소되었습니다.",
                        NotificationRefType.DELIVERY);
            }
        }

        // 상점주에게 SSE 실시간 알림 (배달 상태 변경 시 대시보드 갱신용)
        if (event.getStoreOwnerId() != null) {
            Map<String, Object> sseData = Map.of(
                    "deliveryId", event.getDeliveryId(),
                    "status", event.getNewStatus().name());
            sseService.send(event.getStoreOwnerId(), SseEventType.DELIVERY_STATUS_CHANGED, sseData);
        }
    }
}
