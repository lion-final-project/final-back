package com.example.finalproject.payment.scheduler;


import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 새벽 2시에 자동결제 실행
 * <p>
 * 조건: - ACTIVE 상태 - nextPaymentDate <= 오늘
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionBillingScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionRecurringProcessor recurringProcessor;

    @Scheduled(cron = "0 0 2 * * *")
    public void processRecurringPayments() {

        LocalDate today = LocalDate.now();

        List<Long> targetIds =
                subscriptionRepository.findIdsByStatusAndNextPaymentDateLessThanEqual(
                        SubscriptionStatus.ACTIVE,
                        today);

        if (targetIds.isEmpty()) {
            log.info("자동결제 대상 없음 - {}", today);
            return;
        }

        log.info("자동결제 대상 {}건 시작", targetIds.size());

        for (Long id : targetIds) {
            try {
                recurringProcessor.processSingleSubscription(id);
            } catch (Exception e) {
                log.error("자동결제 처리 중 오류 - subscriptionId={}", id, e);
            }
        }

        log.info("자동결제 스케줄 종료");
    }
}


