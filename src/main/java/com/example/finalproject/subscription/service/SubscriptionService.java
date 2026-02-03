package com.example.finalproject.subscription.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.dto.response.GetSubscriptionResponse;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final Set<SubscriptionStatus> LISTABLE_STATUSES =
            EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED, SubscriptionStatus.CANCELLATION_PENDING);

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 고객의 구독 목록을 조회한다 (API-SUB-002). 해지 완료(CANCELLED)는 제외.
     *
     * @param userId 로그인한 사용자 ID
     * @return 구독 목록 (구독 상품, 구독 상태, 다음 결제 예정일 등)
     */
    @Transactional(readOnly = true)
    public List<GetSubscriptionResponse> findListByUserId(Long userId) {
        List<Subscription> list = subscriptionRepository.findByUser_IdAndStatusInOrderByCreatedAtDesc(userId, LISTABLE_STATUSES);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 구독을 일시정지한다 (API-SUB-003). 본인 구독이며 ACTIVE 상태일 때만 가능.
     *
     * @param subscriptionId 구독 ID
     * @param userId         로그인한 사용자 ID
     * @throws BusinessException 구독 없음(SUBSCRIPTION_NOT_FOUND), 본인 구독 아님(SUBSCRIPTION_FORBIDDEN), 상태 불가(SUBSCRIPTION_INVALID_STATUS)
     */
    @Transactional
    public void pause(Long subscriptionId, Long userId) {
        Subscription subscription = getOwnSubscription(subscriptionId, userId);
        try {
            subscription.pause();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_INVALID_STATUS);
        }
    }

    /**
     * 일시정지된 구독을 재개한다 (API-SUB-004). 본인 구독이며 PAUSED 상태일 때만 가능.
     *
     * @param subscriptionId 구독 ID
     * @param userId         로그인한 사용자 ID
     * @throws BusinessException 구독 없음, 본인 구독 아님, 상태 불가
     */
    @Transactional
    public void resume(Long subscriptionId, Long userId) {
        Subscription subscription = getOwnSubscription(subscriptionId, userId);
        try {
            subscription.resume();
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_INVALID_STATUS);
        }
    }

    /**
     * 구독 해지를 요청한다 (API-SUB-005). 다음 결제일 기준 해지 정책에 따라 해지 예정(CANCELLATION_PENDING)으로 전환한다.
     * 본인 구독이며 ACTIVE 또는 PAUSED 상태일 때만 가능.
     *
     * @param subscriptionId 구독 ID
     * @param userId         로그인한 사용자 ID
     * @param reason         해지 사유 (선택, null 가능)
     * @throws BusinessException 구독 없음, 본인 구독 아님, 상태 불가
     */
    @Transactional
    public void cancel(Long subscriptionId, Long userId, String reason) {
        Subscription subscription = getOwnSubscription(subscriptionId, userId);
        try {
            subscription.requestCancellation(reason);
        } catch (IllegalStateException e) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_INVALID_STATUS);
        }
    }

    /**
     * 구독 ID와 사용자 ID로 본인 구독을 조회한다. 없거나 소유자가 아니면 예외.
     */
    private Subscription getOwnSubscription(Long subscriptionId, Long userId) {
        return subscriptionRepository.findByIdAndUser_Id(subscriptionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
    }

    private GetSubscriptionResponse toResponse(Subscription s) {
        return GetSubscriptionResponse.builder()
                .subscriptionId(s.getId())
                .storeId(s.getStore().getId())
                .storeName(s.getStore().getStoreName())
                .subscriptionProductId(s.getSubscriptionProduct().getId())
                .subscriptionProductName(s.getSubscriptionProduct().getSubscriptionProductName())
                .status(s.getStatus())
                .totalAmount(s.getTotalAmount())
                .deliveryTimeSlot(s.getDeliveryTimeSlot())
                .nextPaymentDate(s.getNextPaymentDate())
                .startedAt(s.getStartedAt())
                .pausedAt(s.getPausedAt())
                .cancelledAt(s.getCancelledAt())
                .cancelReason(s.getCancelReason())
                .build();
    }
}
