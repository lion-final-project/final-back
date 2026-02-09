package com.example.finalproject.checkout.controller;

import com.example.finalproject.checkout.dto.response.GetCheckoutResponse;
import com.example.finalproject.checkout.service.CheckoutService;
import com.example.finalproject.global.response.ApiResponse;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // 주문서 미리보기 조회 쿠폰/할인/포인트 반영, null/0 허용
    @GetMapping
    public ResponseEntity<ApiResponse<GetCheckoutResponse>> getCheckout(
            Authentication authentication,
            @RequestParam String cartItemIds,
            @RequestParam(required = false) Long addressId,
            @RequestParam(required = false) Long couponId,
            @RequestParam(required = false) Integer usePoints
    ) {
        // 장바구니 상품 ID 목록 파싱
        List<Long> ids = Arrays.stream(cartItemIds.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();
        log.info("[결제창] 주문서 미리보기 요청. 사용자={}, 장바구니 상품={}건, 배송지ID={}, 쿠폰ID={}, 사용포인트={}원",
                authentication.getName(), ids.size(), addressId, couponId, usePoints != null ? usePoints : 0);
        GetCheckoutResponse response = checkoutService.getCheckout(
                authentication.getName(), ids, addressId, couponId, usePoints);
        return ResponseEntity.ok(ApiResponse.success("주문서 미리보기 조회가 완료되었습니다.", response));
    }
}
