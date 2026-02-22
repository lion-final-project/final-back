package com.example.finalproject.payment.client;


import com.example.finalproject.payment.client.config.TossFeignConfig;
import com.example.finalproject.payment.dto.request.TossBillingApproveRequest;
import com.example.finalproject.payment.dto.request.TossBillingKeyIssueRequest;
import com.example.finalproject.payment.dto.request.TossCancelRequest;
import com.example.finalproject.payment.dto.request.TossConfirmRequest;
import com.example.finalproject.payment.dto.response.TossBillingApproveResponse;
import com.example.finalproject.payment.dto.response.TossBillingKeyIssueResponse;
import com.example.finalproject.payment.dto.response.TossCancelResponse;
import com.example.finalproject.payment.dto.response.TossConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    /**
     * billingKey 발급 POST /v1/billing/authorizations/{authKey}
     */
    @PostMapping("/v1/billing/authorizations/{authKey}")
    TossBillingKeyIssueResponse issueBillingKey(
            @PathVariable("authKey") String authKey,
            @RequestBody TossBillingKeyIssueRequest request
    );

    @DeleteMapping("/v1/billing/{billingKey}")
    void deleteBillingKey(@PathVariable String billingKey);

    /**
     * POST /v1/billing/{billingKey}
     */
    @PostMapping("/v1/billing/{billingKey}")
    TossBillingApproveResponse approveBilling(
            @PathVariable("billingKey") String billingKey,
            @RequestBody TossBillingApproveRequest request
    );

}
