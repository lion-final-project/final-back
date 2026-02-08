package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.request.PatchCartUpdateRequest;
import com.example.finalproject.order.dto.request.PostCartAddRequest;
import com.example.finalproject.order.dto.response.GetCartResponse;
import com.example.finalproject.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<GetCartResponse>> add(
            Authentication authentication,
            @RequestBody @Valid PostCartAddRequest request) {

        GetCartResponse cartResponse = cartService.addToCart(authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cartResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<GetCartResponse>> getMyCart(Authentication authentication) {
        log.info("[장바구니] 장바구니 조회 요청. 사용자={}", authentication.getName());
        GetCartResponse cartResponse = cartService.getMyCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(cartResponse));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<GetCartResponse>> updateQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody @Valid PatchCartUpdateRequest request) {

        GetCartResponse cartResponse = cartService.updateQuantity(authentication.getName(), productId, request);
        return ResponseEntity.ok(ApiResponse.success(cartResponse));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<GetCartResponse>> removeItem(
            Authentication authentication,
            @PathVariable Long productId) {

        GetCartResponse cartResponse = cartService.removeItem(authentication.getName(), productId);
        return ResponseEntity.ok(ApiResponse.success(cartResponse));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<GetCartResponse>> clear(Authentication authentication) {
        GetCartResponse cartResponse = cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(cartResponse));
    }
}