package com.example.finalproject.store.service;

import com.example.finalproject.delivery.component.DeliveryMatchComponent;
import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.subscription.domain.SubscriptionHistory;
import com.example.finalproject.subscription.enums.SubHistoryStatus;
import com.example.finalproject.subscription.repository.SubscriptionHistoryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마트 구독 배송 접수 처리.
 * 점주가 "배송 접수" 버튼을 누르면 PENDING → ACCEPTED → READY 전환 후 라이더 배차 알림을 전송한다.
 */
@Service
@RequiredArgsConstructor
public class StoreSubscriptionDeliveryService {

    private static final List<String> VALID_TIME_SLOTS =
            List.of("08:00~11:00", "11:00~14:00", "14:00~17:00", "17:00~20:00");

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMatchComponent deliveryMatchComponent;

    /**
     * 해당 날짜·시간대의 구독 주문을 일괄 접수하고 라이더 배차 알림을 전송한다.
     * StoreOrder: PENDING → ACCEPTED → READY, Delivery 생성 후 주변 라이더에게 SSE 전송.
     *
     * @param storeId          마트 ID
     * @param scheduledDate    배송 예정일 (yyyy-MM-dd)
     * @param deliveryTimeSlot 시간대 (예: 08:00~11:00)
     * @return 접수 처리된 store_order 건수
     */
    @Transactional
    public int acceptSubscriptionDeliveries(Long storeId, LocalDate scheduledDate, String deliveryTimeSlot) {
        if (deliveryTimeSlot == null || !VALID_TIME_SLOTS.contains(deliveryTimeSlot)) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_INVALID_DELIVERY_TIME_SLOT);
        }

        List<SubscriptionHistory> histories = subscriptionHistoryRepository
                .findBySubscription_Store_IdAndScheduledDateAndSubscription_DeliveryTimeSlotAndStatus(
                        storeId, scheduledDate, deliveryTimeSlot, SubHistoryStatus.ORDERED);

        List<StoreOrder> toAccept = histories.stream()
                .map(SubscriptionHistory::getStoreOrder)
                .filter(so -> so != null && so.getStatus() == StoreOrderStatus.PENDING)
                .collect(Collectors.toList());

        for (StoreOrder so : toAccept) {
            so.accept();
            so.markReady();

            Delivery delivery = deliveryRepository.save(
                    Delivery.builder()
                            .storeOrder(so)
                            .storeLocation(so.getStore().getAddress().getLocation())
                            .customerLocation(so.getOrder().getDeliveryLocation())
                            .deliveryFee(so.getDeliveryFee())
                            .build());
            deliveryMatchComponent.notifyNewDelivery(delivery);
        }
        return toAccept.size();
    }
}
