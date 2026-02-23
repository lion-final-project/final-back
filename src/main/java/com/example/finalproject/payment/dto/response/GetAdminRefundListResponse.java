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
public class GetAdminRefundListResponse {

    private Long refundId;
    private Long storeOrderId;
    private Long storeId;
    private String storeName;
    private Long customerId;
    private String customerName;
    private Integer refundAmount;
    private RefundStatus refundStatus;
    private RefundResponsibility responsibility;
    private LocalDateTime requestedAt;
    
    @Builder
    public GetAdminRefundListResponse(Long refundId, Long storeOrderId, Long storeId, String storeName,
                                      Long customerId, String customerName, Integer refundAmount,
                                      RefundStatus refundStatus, RefundResponsibility responsibility,
                                      LocalDateTime requestedAt) {
        this.refundId = refundId;
        this.storeOrderId = storeOrderId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.customerId = customerId;
        this.customerName = customerName;
        this.refundAmount = refundAmount;
        this.refundStatus = refundStatus;
        this.responsibility = responsibility;
        this.requestedAt = requestedAt;
    }

    public static GetAdminRefundListResponse from(PaymentRefund refund) {
        return GetAdminRefundListResponse.builder()
                .refundId(refund.getId())
                .storeOrderId(refund.getStoreOrder().getId())
                .storeId(refund.getStoreOrder().getStore().getId())
                .storeName(refund.getStoreOrder().getStore().getStoreName())
                .customerId(refund.getStoreOrder().getOrder().getUser().getId())
                .customerName(refund.getStoreOrder().getOrder().getUser().getName())
                .refundAmount(refund.getRefundAmount())
                .refundStatus(refund.getRefundStatus())
                .responsibility(refund.getResponsibility())
                .requestedAt(refund.getCreatedAt())
                .build();
    }
}
