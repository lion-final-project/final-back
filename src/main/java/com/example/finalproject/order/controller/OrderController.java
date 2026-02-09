package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.request.PostOrderRequest;
import com.example.finalproject.order.dto.response.GetOrderDetailResponse;
import com.example.finalproject.order.dto.response.PostOrderResponse;
import com.example.finalproject.order.service.OrderCreateService;
import com.example.finalproject.order.service.OrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;
    private final OrderCreateService orderCreateService;

    // 주문 생성 
    @PostMapping
    public ResponseEntity<ApiResponse<PostOrderResponse>> createOrder(
            Authentication authentication,
            @RequestBody @Valid PostOrderRequest request) {
        log.info("[주문] 주문 생성 요청. 사용자={}, 상품={}건, 배송지ID={}, 결제수단ID={}, 쿠폰ID={}, 사용포인트={}원",
                authentication.getName(), request.getCartItemIds() != null ? request.getCartItemIds().size() : 0,
                request.getAddressId(), request.getPaymentMethodId(), request.getCouponId(),
                request.getUsePoints() != null ? request.getUsePoints() : 0);
        PostOrderResponse response = orderCreateService.createOrder(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("주문이 생성되었습니다.", response));
    }

    // 주문 상세 조회
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
