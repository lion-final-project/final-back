package com.example.finalproject.payment.service.pg;

import com.example.finalproject.payment.client.TossPaymentsClient;
import com.example.finalproject.payment.dto.request.TossCancelRequest;
import com.example.finalproject.payment.dto.response.TossCancelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateWay {

    private final TossPaymentsClient tossPaymentsClient;

    @Override
    public CancelResult cancel(String externalPaymentId, int amount, String reason) {

        TossCancelResponse response =
                tossPaymentsClient.cancel(externalPaymentId, new TossCancelRequest(reason, amount));

        return new CancelResult(response.getCumulativeCanceledAmount());
    }
}

