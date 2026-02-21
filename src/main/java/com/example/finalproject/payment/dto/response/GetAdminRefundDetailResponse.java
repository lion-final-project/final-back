package com.example.finalproject.payment.dto.response;

import com.example.finalproject.payment.domain.PaymentRefund;
import com.example.finalproject.payment.enums.RefundResponsibility;
import com.example.finalproject.payment.enums.RefundStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetAdminRefundDetailResponse {

    private Long refundId;
    private Long storeOrderId;
    private Long storeId;
    private String storeName;
    private Long customerId;
    private String customerName;
    private Long paymentId;
    private Integer refundAmount;
    private String refundReason;
    private RefundStatus refundStatus;
    private RefundResponsibility responsibility;
    private LocalDateTime requestedAt;
    private LocalDateTime refundedAt;

    @Builder
    public GetAdminRefundDetailResponse(Long refundId, Long storeOrderId, Long storeId, String storeName,
                                        Long customerId, String customerName, Long paymentId,
                                        Integer refundAmount, String refundReason, RefundStatus refundStatus,
                                        RefundResponsibility responsibility, LocalDateTime requestedAt,
                                        LocalDateTime refundedAt) {
        this.refundId = refundId;
        this.storeOrderId = storeOrderId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.paymentId = paymentId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundStatus = refundStatus;
        this.responsibility = responsibility;
        this.requestedAt = requestedAt;
        this.refundedAt = refundedAt;
    }

    public static GetAdminRefundDetailResponse from(PaymentRefund refund) {
        return GetAdminRefundDetailResponse.builder()
                .refundId(refund.getId())
                .storeOrderId(refund.getStoreOrder().getId())
                .storeId(refund.getStoreOrder().getStore().getId())
                .storeName(refund.getStoreOrder().getStore().getStoreName())
                .customerId(refund.getStoreOrder().getOrder().getUser().getId())
                .customerName(refund.getStoreOrder().getOrder().getUser().getName())
                .paymentId(refund.getPayment().getId())
                .refundAmount(refund.getRefundAmount())
                .refundReason(refund.getRefundReason())
                .refundStatus(refund.getRefundStatus())
                .responsibility(refund.getResponsibility())
                .requestedAt(refund.getCreatedAt())
                .refundedAt(refund.getRefundedAt())
                .build();
    }
}
