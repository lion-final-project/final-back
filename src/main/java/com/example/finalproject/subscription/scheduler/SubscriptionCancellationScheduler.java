package com.example.finalproject.subscription.scheduler;

import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import com.example.finalproject.subscription.service.SubscriptionCancellationProcessor;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 02:30 KST에 해지 예정(CANCELLATION_PENDING) 구독 중 nextPaymentDate가 도래한 건을 CANCELLED로 전환한다.
 * 결제 스케줄러(02:00) 이후 실행하여 당일 결제 대상과 충돌하지 않도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionCancellationScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionCancellationProcessor cancellationProcessor;

    @Scheduled(cron = "0 30 2 * * *", zone = "Asia/Seoul")
    public void processPendingCancellations() {
        LocalDate today = LocalDate.now();
        log.info("구독 자동 해지 배치 시작: date={}", today);

        List<Long> ids = subscriptionRepository.findIdsByStatusAndNextPaymentDateLessThanEqual(
                SubscriptionStatus.CANCELLATION_PENDING, today);

        if (ids.isEmpty()) {
            log.info("구독 자동 해지 배치 완료: 대상 없음");
            return;
        }

        log.info("구독 자동 해지 대상: {}건", ids.size());
        int cancelled = 0;
        for (Long id : ids) {
            try {
                cancellationProcessor.processSingleCancellation(id);
                cancelled++;
            } catch (Exception e) {
                log.error("구독 자동 해지 실패: subscriptionId={}", id, e);
            }
        }
        log.info("구독 자동 해지 배치 완료: {}건/{}", cancelled, ids.size());
    }
}
