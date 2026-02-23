package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.request.PostOrderCancelRequest;
import com.example.finalproject.order.service.StoreOrderRefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store-orders")
public class StoreOrderRefundController {

    private final StoreOrderRefundService storeOrderRefundService;

    /**
     * 스토어 주문 환불 요청
     *
     * @param authentication 현재 로그인한 사용자 인증 정보
     * @param storeOrderId   환불 대상 스토어 주문 ID
     * @param request        환불 사유를 포함한 요청 DTO
     * @return 200 OK
     */

    @PostMapping("/{storeOrderId}/refund")
    public ResponseEntity<ApiResponse<Void>> requestRefund(
            Authentication authentication,
            @PathVariable Long storeOrderId,
            @RequestBody @Valid PostOrderCancelRequest request) {

        storeOrderRefundService.requestRefund(
                authentication.getName(),
                storeOrderId,
                request
        );

        return ResponseEntity.ok(ApiResponse.success("환불 요청이 접수되었습니다."));
    }
}