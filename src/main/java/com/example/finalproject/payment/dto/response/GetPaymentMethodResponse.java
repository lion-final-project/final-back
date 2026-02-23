package com.example.finalproject.payment.dto.response;

import com.example.finalproject.payment.domain.PaymentMethod;
import lombok.Getter;

@Getter
public class GetPaymentMethodResponse {
    private final Long id;
    private final String methodType;
    private final String cardCompany;
    private final String cardNumberMasked;
    private final Boolean isDefault;

    public GetPaymentMethodResponse(PaymentMethod paymentMethod) {
        this.id = paymentMethod.getId();
        this.methodType = paymentMethod.getMethodType().name();
        this.cardCompany = paymentMethod.getCardCompany();
        this.cardNumberMasked = paymentMethod.getCardNumberMasked();
        this.isDefault = paymentMethod.getIsDefault();
    }
}
