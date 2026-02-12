package com.example.finalproject.user.withdrawal.rule;

import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.subscription.repository.SubscriptionRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.withdrawal.dto.BlockedReason;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveSubscriptionWithdrawalRule implements WithdrawalEligibilityRule {

    public static final String CODE = "ACTIVE_SUBSCRIPTION_EXISTS";

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public Optional<BlockedReason> validate(User user) {
        long activeSubscriptionCount = subscriptionRepository.countByUserIdAndStatusIn(
                user.getId(),
                List.of(
                        SubscriptionStatus.ACTIVE,
                        SubscriptionStatus.PAUSED,
                        SubscriptionStatus.CANCELLATION_PENDING
                )
        );

        if (activeSubscriptionCount > 0) {
            return Optional.of(BlockedReason.builder()
                    .code(CODE)
                    .message("진행 중인 구독이 있어 탈퇴할 수 없습니다.")
                    .build());
        }
        return Optional.empty();
    }
}
