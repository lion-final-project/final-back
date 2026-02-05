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

    @GetMapping
    public ResponseEntity<ApiResponse<GetCheckoutResponse>> getCheckout(
            Authentication authentication,
            @RequestParam String cartItemIds,
            @RequestParam(required = false) Long addressId
    ) {
        List<Long> ids = Arrays.stream(cartItemIds.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();
        log.debug("getCheckout request: email={}, cartItemIds={}, addressId={}", authentication.getName(), ids, addressId);
        GetCheckoutResponse response = checkoutService.getCheckout(authentication.getName(), ids, addressId);
        return ResponseEntity.ok(ApiResponse.success("주문서 미리보기 조회가 완료되었습니다.", response));
    }
}
