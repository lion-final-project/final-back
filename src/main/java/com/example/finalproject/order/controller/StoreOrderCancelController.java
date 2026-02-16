package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.request.PostOrderCancelRequest;
import com.example.finalproject.order.service.StoreOrderCancelService;
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
public class StoreOrderCancelController {

    private final StoreOrderCancelService storeOrderCancelService;


    /**
     * 스토어 주문 취소 요청
     *
     * @param authentication 현재 로그인한 사용자 인증 정보
     * @param storeOrderId   취소 대상 스토어 주문 ID
     * @param request        취소 사유를 포함한 요청 DTO
     * @return 200 OK
     */
    @PostMapping("/{storeOrderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelStoreOrder(
            Authentication authentication,
            @PathVariable Long storeOrderId,
            @RequestBody @Valid PostOrderCancelRequest request) {

        storeOrderCancelService.cancelStoreOrder(authentication.getName(), storeOrderId, request.getReason());

        return ResponseEntity.ok(ApiResponse.success("주문이 취소되었습니다."));
    }
}
