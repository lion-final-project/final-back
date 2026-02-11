package com.example.finalproject.user.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetWithdrawalCheckResponse {

    private final boolean withdrawable;
    private final List<String> reasons;
    private final long activeSubscriptionCount;
    private final long pendingPaymentCount;
    private final long inProgressOrderCount;
}
