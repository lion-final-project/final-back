package com.example.finalproject.payment.controller;

import com.example.finalproject.payment.service.AdminRefundCommandService;
import com.example.finalproject.payment.service.AdminRefundService;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.payment.dto.request.PostPaymentRefundApproveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/refunds")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;
    private final AdminRefundCommandService adminRefundCommandService;

    @PostMapping("/{refundId}/approve")
    public ApiResponse<Void> approve(
            @PathVariable Long refundId,
            @Valid @RequestBody PostPaymentRefundApproveRequest req) {
        adminRefundService.approveAndCancel(refundId, req);
        return ApiResponse.success(null);
    }

    @PostMapping("/{refundId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long refundId) {
        adminRefundCommandService.reject(refundId);
        return ApiResponse.success(null);
    }
}
