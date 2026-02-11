package com.example.finalproject.user.service;

import com.example.finalproject.auth.repository.RefreshTokenRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.security.CustomUserDetails;
import com.example.finalproject.order.enums.OrderStatus;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import com.example.finalproject.user.dto.response.GetWithdrawalCheckResponse;
import com.example.finalproject.user.dto.response.PostWithdrawalConfirmResponse;
import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Slice<StoreNearbyResponse> getNearbyStores(GetStoreSearchRequest request) {
        return storeRepository.findNearbyStoresByCategory(request);
    }

    //회원탈퇴 가능 여부 조회
    @Override
    @Transactional(readOnly = true)
    public GetWithdrawalCheckResponse checkWithdrawalEligibility(Authentication authentication) {
        User user = getCurrentUser(authentication);
        log.info("[회원탈퇴] 탈퇴 가능 여부 조회 시작 userId={}", user.getId());

        long activeSubscriptionCount = subscriptionRepository.countByUserIdAndStatusIn(
                user.getId(),
                List.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.PAUSED,
                        SubscriptionStatus.CANCELLATION_PENDING
                )
        );
        log.info("[회원탈퇴] 구독 여부 확인 userId={}, 진행중 구독 수={}", user.getId(), activeSubscriptionCount);

        long pendingPaymentCount = paymentRepository.countByOrder_UserIdAndPaymentStatus(
                user.getId(),
                PaymentStatus.PENDING
        );
        log.info("[회원탈퇴] 결제 대기 여부 확인 userId={}, 결제대기 수={}", user.getId(), pendingPaymentCount);

        long inProgressOrderCount = orderRepository.countByUserIdAndStatusIn(
                user.getId(),
                List.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.PARTIAL_CANCELLED)
        );
        log.info("[회원탈퇴] 주문 진행 여부 확인 userId={}, 진행중 주문 수={}", user.getId(), inProgressOrderCount);

        List<String> reasons = new ArrayList<>();
        if (user.getStatus() != UserStatus.ACTIVE) {
            reasons.add("이미 탈퇴했거나 비활성화된 계정입니다.");
        }
        if (activeSubscriptionCount > 0) {
            reasons.add("진행 중인 구독이 있어 탈퇴할 수 없습니다.");
        }
        if (pendingPaymentCount > 0) {
            reasons.add("결제 대기 상태(PENDING)가 있어 탈퇴할 수 없습니다.");
        }
        if (inProgressOrderCount > 0) {
            reasons.add("진행 중인 주문(PENDING/PAID 등)이 있어 탈퇴할 수 없습니다.");
        }

        if (reasons.isEmpty()) {
            log.info("[회원탈퇴] 탈퇴 가능 userId={}", user.getId());
        } else {
            log.warn("[회원탈퇴] 회원탈퇴 불가 userId={}, 사유={}", user.getId(), reasons);
        }

        return GetWithdrawalCheckResponse.builder()
                .withdrawable(reasons.isEmpty())
                .reasons(reasons)
                .activeSubscriptionCount(activeSubscriptionCount)
                .pendingPaymentCount(pendingPaymentCount)
                .inProgressOrderCount(inProgressOrderCount)
                .build();
    }

    //회원탈퇴 처리
    @Override
    @Transactional
    public PostWithdrawalConfirmResponse withdraw(Authentication authentication) {
        User user = getCurrentUser(authentication);
        GetWithdrawalCheckResponse check = checkWithdrawalEligibility(authentication);

        if (!check.isWithdrawable()) {
            log.warn("[회원탈퇴] 회원탈퇴 불가 - 확정 요청 거부 userId={}, 사유={}", user.getId(), check.getReasons());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        user.deactive();
        refreshTokenRepository.deleteByUser(user);
        log.info("[회원탈퇴] 정상 회원탈퇴 완료 userId={}, deletedAt={}", user.getId(), user.getDeletedAt());

        return PostWithdrawalConfirmResponse.builder()
                .userId(user.getId())
                .status(UserStatus.INACTIVE.name())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    //현재 사용자 조회
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return userRepository.findById(details.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }
        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

}
