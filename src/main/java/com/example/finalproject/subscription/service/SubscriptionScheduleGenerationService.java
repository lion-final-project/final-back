package com.example.finalproject.subscription.service;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionDayOfWeek;
import com.example.finalproject.subscription.domain.SubscriptionHistory;
import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.repository.SubscriptionDayOfWeekRepository;
import com.example.finalproject.subscription.repository.SubscriptionHistoryRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 배송 일정(SubscriptionHistory)을 생성한다.
 * 구독 활성화(최초 결제) 및 월 자동 결제 후 다음 사이클의 배송 날짜를 미리 계산하여 SCHEDULED 상태로 저장한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionScheduleGenerationService {

    private final SubscriptionDayOfWeekRepository dayOfWeekRepository;
    private final SubscriptionHistoryRepository historyRepository;

    /**
     * 주어진 구독의 periodStart부터 SUBSCRIPTION_PERIOD_DAYS(28일)간 배송 일정을 생성한다.
     * 이미 해당 기간에 SCHEDULED 이력이 있는 날짜는 건너뛴다(중복 방지).
     *
     * @param subscription 대상 구독
     * @param periodStart  배송 일정 시작일 (보통 결제일 당일)
     */
    @Transactional
    public void generateSchedule(Subscription subscription, LocalDate periodStart) {
        List<SubscriptionDayOfWeek> days = dayOfWeekRepository.findBySubscription(subscription);
        if (days.isEmpty()) {
            log.warn("구독 배송 요일 없음 - subscriptionId={}", subscription.getId());
            return;
        }

        Set<Short> deliveryDays = days.stream()
                .map(d -> d.getId().getDayOfWeek())
                .collect(Collectors.toSet());

        LocalDate periodEnd = periodStart.plusDays(SubscriptionProduct.SUBSCRIPTION_PERIOD_DAYS - 1);

        // 이미 생성된 날짜 집합 (중복 방지)
        Set<LocalDate> existing = historyRepository
                .findBySubscriptionAndScheduledDateBetween(subscription, periodStart, periodEnd)
                .stream()
                .map(SubscriptionHistory::getScheduledDate)
                .collect(Collectors.toSet());

        int cycleBase = subscription.getCycleCount() != null ? subscription.getCycleCount() : 0;
        int seq = 0;

        LocalDate current = periodStart;
        while (!current.isAfter(periodEnd)) {
            short dow = (short) current.getDayOfWeek().getValue(); // ISO-8601: 월=1 ~ 일=7
            if (deliveryDays.contains(dow) && !existing.contains(current)) {
                historyRepository.save(
                        SubscriptionHistory.builder()
                                .subscription(subscription)
                                .cycleCount(cycleBase + seq + 1)
                                .scheduledDate(current)
                                .build());
                seq++;
            }
            current = current.plusDays(1);
        }

        log.info("구독 배송 일정 생성: subscriptionId={}, 기간={}/{}~{}, 생성={}건",
                subscription.getId(), periodStart, periodEnd, periodEnd, seq);
    }
}
