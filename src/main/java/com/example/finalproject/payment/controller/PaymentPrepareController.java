package com.example.finalproject.payment.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.payment.dto.request.PostPaymentConfirmRequest;
import com.example.finalproject.payment.dto.request.PostPaymentPrepareRequest;
import com.example.finalproject.payment.dto.response.PostPaymentConfirmResponse;
import com.example.finalproject.payment.dto.response.PostPaymentPrepareResponse;
import com.example.finalproject.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentPrepareController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public ResponseEntity<ApiResponse<PostPaymentPrepareResponse>> prepare(
            Authentication authentication,
            @RequestBody @Valid PostPaymentPrepareRequest request) {
        PostPaymentPrepareResponse postPaymentPrepareResponse = paymentService.prepare(authentication.getName(),
                request);
        return ResponseEntity.ok(ApiResponse.success(postPaymentPrepareResponse));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PostPaymentConfirmResponse>> confirm(
            Authentication authentication,
            @RequestBody @Valid PostPaymentConfirmRequest request) {
        PostPaymentConfirmResponse confirm = paymentService.confirm(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(confirm));
    }
}

