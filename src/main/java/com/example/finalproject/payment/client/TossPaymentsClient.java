package com.example.finalproject.payment.client;


import com.example.finalproject.payment.client.config.TossFeignConfig;
import com.example.finalproject.payment.dto.request.TossCancelRequest;
import com.example.finalproject.payment.dto.request.TossConfirmRequest;
import com.example.finalproject.payment.dto.response.TossCancelResponse;
import com.example.finalproject.payment.dto.response.TossConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "tossPaymentsClient",
        url = "${toss.payments.base-url}",
        configuration = TossFeignConfig.class
)
public interface TossPaymentsClient {

    @PostMapping("/v1/payments/confirm")
    TossConfirmResponse confirm(@RequestBody TossConfirmRequest request);

    @PostMapping("/v1/payments/{paymentKey}/cancel")
    TossCancelResponse cancel(
            @PathVariable("paymentKey") String paymentKey,
            @RequestBody TossCancelRequest request
    );
}
