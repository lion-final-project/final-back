package com.example.finalproject.subscription.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionStatusService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionScheduleGenerationService scheduleGenerationService;

    @Transactional
    public void activateAfterFirstPayment(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.activate();

        // 최초 결제 완료 → 첫 사이클 배송 일정 생성
        scheduleGenerationService.generateSchedule(subscription, LocalDate.now());
    }

    @Transactional
    public void markPaymentFailed(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        subscription.markPaymentFailed();
    }
}
