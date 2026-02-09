package com.example.finalproject.subscription.scheduler;

import com.example.finalproject.subscription.service.SubscriptionOrderCreationService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 구독 배송 예정일 당일 자정에 SCHEDULED 구독 이력을 주문(Order/StoreOrder)으로 생성한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionOrderCreationScheduler {

    private final SubscriptionOrderCreationService subscriptionOrderCreationService;

    /** 매일 00:05 KST 에 오늘 예정된 구독 주문 생성 */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void createSubscriptionOrdersForToday() {
        LocalDate today = LocalDate.now();
        log.info("구독 주문 자동 생성 배치 시작: scheduledDate={}", today);
        int count = subscriptionOrderCreationService.createOrdersForScheduledDate(today);
        log.info("구독 주문 자동 생성 배치 완료: 생성 건수={}", count);
    }
}
