package com.example.finalproject.user.withdrawal.rule;

import com.example.finalproject.order.enums.OrderStatus;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.withdrawal.dto.BlockedReason;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InProgressOrderWithdrawalRule implements WithdrawalEligibilityRule {

    public static final String CODE = "IN_PROGRESS_ORDER_EXISTS";

    private final OrderRepository orderRepository;

    @Override
    public Optional<BlockedReason> validate(User user) {
        long inProgressOrderCount = orderRepository.countByUserIdAndStatusIn(
                user.getId(),
                List.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.PARTIAL_CANCELLED)
        );

        if (inProgressOrderCount > 0) {
            return Optional.of(BlockedReason.builder()
                    .code(CODE)
                    .message("진행 중인 주문이 있어 탈퇴할 수 없습니다.")
                    .build());
        }
        return Optional.empty();
    }
}
