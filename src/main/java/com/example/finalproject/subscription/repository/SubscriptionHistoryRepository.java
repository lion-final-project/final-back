package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionHistory;
import com.example.finalproject.subscription.enums.SubHistoryStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {

    /**
     * 구독의 배송 이력 중 특정 상태인 건수를 센다 (UC-C10 남은 배송 건수 등 계산용).
     *
     * @param subscription 구독
     * @param status       이력 상태 (SCHEDULED, ORDERED, COMPLETED, SKIPPED)
     * @return 해당 상태 건수
     */
    long countBySubscriptionAndStatus(Subscription subscription, SubHistoryStatus status);

    /**
     * 특정 상태·예정일인 구독 이력을 조회한다 (구독 주문 자동 생성 배치용).
     *
     * @param status       SCHEDULED
     * @param scheduledDate 예정 배송일
     * @return 해당 이력 목록
     */
    List<SubscriptionHistory> findByStatusAndScheduledDate(SubHistoryStatus status, LocalDate scheduledDate);

    /**
     * 마트·날짜·시간대·상태로 구독 이력을 조회한다 (배송 접수 시 해당 store_orders 조회용).
     *
     * @param storeId        마트 ID
     * @param scheduledDate  배송 예정일
     * @param deliveryTimeSlot 시간대 (예: 08:00~11:00)
     * @param status         ORDERED (이미 주문 생성된 건)
     * @return 해당 이력 목록 (store_order_id 있음)
     */
    List<SubscriptionHistory> findBySubscription_Store_IdAndScheduledDateAndSubscription_DeliveryTimeSlotAndStatus(
            Long storeId, LocalDate scheduledDate, String deliveryTimeSlot, SubHistoryStatus status);

    /**
     * StoreOrder에 연결된 구독 이력을 조회한다 (배송 완료 시 subscription_history 상태를 COMPLETED로 변경할 때 사용).
     * 구독 주문은 StoreOrder당 1건의 SubscriptionHistory만 가지므로 Optional로 반환한다.
     *
     * @param storeOrderId StoreOrder ID
     * @return 해당 구독 이력 (없으면 empty)
     */
    @Query("SELECT sh FROM SubscriptionHistory sh WHERE sh.storeOrder.id = :storeOrderId")
    Optional<SubscriptionHistory> findFirstByStoreOrderId(@Param("storeOrderId") Long storeOrderId);

    /**
     * 구독의 특정 기간 내 배송 이력을 조회한다 (일정 생성 시 중복 방지용).
     *
     * @param subscription  구독
     * @param startDate     시작일 (inclusive)
     * @param endDate       종료일 (inclusive)
     * @return 해당 기간 내 이력 목록
     */
    List<SubscriptionHistory> findBySubscriptionAndScheduledDateBetween(
            Subscription subscription, LocalDate startDate, LocalDate endDate);
}
