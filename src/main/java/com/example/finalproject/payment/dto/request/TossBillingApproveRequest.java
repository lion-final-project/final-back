package com.example.finalproject.payment.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossBillingApproveRequest {
    private Integer amount;
    private String customerKey;
    private String orderId;
    private String orderName;

    private String customerEmail;
    private String customerName;
}

