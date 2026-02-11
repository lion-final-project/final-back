package com.example.finalproject.payment.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.payment.dto.request.PostBillingKeyIssueRequest;
import com.example.finalproject.payment.dto.response.GetPaymentMethodResponse;
import com.example.finalproject.payment.dto.response.PostBillingKeyIssueResponse;
import com.example.finalproject.payment.service.BillingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    /**
     * 카드 자동결제 billingKey 발급
     */
    @PostMapping("/card")
    public ResponseEntity<ApiResponse<PostBillingKeyIssueResponse>> issueCardBillingKey(
            Authentication authentication,
            @RequestBody @Valid PostBillingKeyIssueRequest request) {

        PostBillingKeyIssueResponse response =
                billingService.issueCardBillingKey(authentication.getName(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 결제 수단 목록 조회
     */
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<GetPaymentMethodResponse>>> getMyPaymentMethods(
            Authentication authentication) {
        List<GetPaymentMethodResponse> paymentMethods =
                billingService.getMyPaymentMethods(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(paymentMethods));
    }

    /**
     * 기본 결제 수단 설정
     */
    @PatchMapping("/methods/{paymentMethodId}/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultPaymentMethod(
            Authentication authentication,
            @PathVariable Long paymentMethodId) {
        billingService.setDefaultPaymentMethod(authentication.getName(), paymentMethodId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 결제 수단(빌링키) 삭제
     */
    @DeleteMapping("/methods/{paymentMethodId}")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            Authentication authentication,
            @PathVariable Long paymentMethodId) {
        billingService.deletePaymentMethod(authentication.getName(), paymentMethodId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}