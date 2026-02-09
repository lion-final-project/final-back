package com.example.finalproject.store.service;

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
 * 마트 구독 배송 접수(ACCEPTED 전환) 처리.
 */
@Service
@RequiredArgsConstructor
public class StoreSubscriptionDeliveryService {

    private static final List<String> VALID_TIME_SLOTS =
            List.of("08:00~11:00", "11:00~14:00", "14:00~17:00", "17:00~20:00");

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    /**
     * 해당 날짜·시간대의 구독 주문(이미 생성된 store_orders)을 일괄 접수(ACCEPTED) 처리한다.
     *
     * @param storeId         마트 ID
     * @param scheduledDate   배송 예정일 (yyyy-MM-dd)
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
        }
        return toAccept.size();
    }
}
