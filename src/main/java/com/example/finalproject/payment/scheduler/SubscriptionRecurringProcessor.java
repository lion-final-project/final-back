package com.example.finalproject.payment.scheduler;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.payment.service.SubscriptionBillingService;
import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import com.example.finalproject.subscription.service.SubscriptionScheduleGenerationService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionRecurringProcessor {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionBillingService subscriptionBillingService;
    private final SubscriptionScheduleGenerationService scheduleGenerationService;

    @Transactional
    public void processSingleSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        try {
            subscriptionBillingService.chargeMonthlyFee(subscription);
            subscription.moveNextBillingDate();

            // 결제 성공 → 다음 사이클 배송 일정 생성
            scheduleGenerationService.generateSchedule(subscription, LocalDate.now());

        } catch (Exception e) {
            subscription.markPaymentFailed();
            throw e;
        }
    }
}
