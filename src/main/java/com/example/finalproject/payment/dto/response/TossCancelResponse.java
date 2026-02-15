package com.example.finalproject.payment.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossCancelResponse {

    private String paymentKey;
    private String status;

    private Integer totalAmount;
    private Integer balanceAmount;

    private List<CancelDetail> cancels;

    @Getter
    @NoArgsConstructor
    public static class CancelDetail {
        private Integer cancelAmount;
        private String cancelReason;
        private String canceledAt;
    }

    public Integer getCumulativeCanceledAmount() {
        if (totalAmount == null || balanceAmount == null) {
            return 0;
        }
        return totalAmount - balanceAmount;
    }
}

