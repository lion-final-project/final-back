package com.example.finalproject.payment.dto.request;

import com.example.finalproject.payment.enums.RefundResponsibility;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostPaymentRefundApproveRequest {
    @NotNull
    private RefundResponsibility responsibility;
}
