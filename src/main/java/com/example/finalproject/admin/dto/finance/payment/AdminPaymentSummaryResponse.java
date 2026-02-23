package com.example.finalproject.admin.dto.finance.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPaymentSummaryResponse {
    private long grossPaymentAmount;
    private long platformFeeRevenue;
    private long refundAmount;
    private long netRevenue;
    private long paymentCount;
    private long refundRequestedCount;
    private long refundApprovedCount;
    private long refundRejectedCount;
    private long refundRequestedAmount;
    private long refundApprovedAmount;
    private long refundRejectedAmount;
    private long regularSalesAmount;
    private long subscriptionSalesAmount;
}
