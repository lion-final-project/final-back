package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.response.GetOrderDetailResponse;
import com.example.finalproject.order.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<GetOrderDetailResponse>> getOrderDetail(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        log.debug("getOrderDetail request: email={}, orderId={}", authentication.getName(), orderId);
        GetOrderDetailResponse response = orderQueryService.getOrderDetail(authentication.getName(), orderId);
        return ResponseEntity.ok(ApiResponse.success("주문 상세 조회가 완료되었습니다.", response));
    }
}
