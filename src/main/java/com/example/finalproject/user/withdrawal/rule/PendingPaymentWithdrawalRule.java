package com.example.finalproject.user.withdrawal.rule;

import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.withdrawal.dto.BlockedReason;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingPaymentWithdrawalRule implements WithdrawalEligibilityRule {

    public static final String CODE = "PENDING_PAYMENT_EXISTS";

    private final PaymentRepository paymentRepository;

    @Override
    public Optional<BlockedReason> validate(User user) {
        long pendingPaymentCount = paymentRepository.countByOrder_UserIdAndPaymentStatus(
                user.getId(),
                PaymentStatus.PENDING
        );

        if (pendingPaymentCount > 0) {
            return Optional.of(BlockedReason.builder()
                    .code(CODE)
                    .message("결제 대기 중인 상품이 있어 탈퇴할 수 없습니다.")
                    .build());
        }
        return Optional.empty();
    }
}
