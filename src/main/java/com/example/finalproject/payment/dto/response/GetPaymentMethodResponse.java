package com.example.finalproject.payment.dto.response;

import com.example.finalproject.payment.domain.PaymentMethod;
import lombok.Getter;

@Getter
public class GetPaymentMethodResponse {
    private Long id;
    private String methodType;
    private String cardCompany;
    private String cardNumberMasked;
    private Boolean isDefault;

    public GetPaymentMethodResponse(PaymentMethod paymentMethod) {
        this.id = paymentMethod.getId();
        this.methodType = paymentMethod.getMethodType().name();
        this.cardCompany = paymentMethod.getCardCompany();
        this.cardNumberMasked = paymentMethod.getCardNumberMasked();
        this.isDefault = paymentMethod.getIsDefault();
    }
}
