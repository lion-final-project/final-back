package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.request.PostCartAddRequest;
import com.example.finalproject.order.dto.request.PatchCartUpdateRequest;
import com.example.finalproject.order.dto.response.GetCartResponse;
import com.example.finalproject.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<GetCartResponse>> add(//@AuthenticationPrincipal Long userId,
                                                            @RequestBody @Valid PostCartAddRequest request) {

        Long userId = 1L;

        /**
         * 배달비 계산 로직 필요
         */
        GetCartResponse cartResponse = cartService.addToCart(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cartResponse));
    }

    @GetMapping
    public GetCartResponse getMyCart(//@AuthenticationPrincipal Long userId
    ) {
        Long userId = 1L;

        return cartService.getMyCart(userId);
    }

    @PatchMapping("/items/{productId}")
    public GetCartResponse updateQuantity(//@AuthenticationPrincipal Long userId,
                                          @PathVariable Long productId,
                                          @RequestBody @Valid PatchCartUpdateRequest request) {
        Long userId = 1L;

        return cartService.updateQuantity(userId, productId, request);
    }

    @DeleteMapping("/items/{productId}")
    public GetCartResponse removeItem(//@AuthenticationPrincipal Long userId,
                                      @PathVariable Long productId) {

        Long userId = 1L;

        return cartService.removeItem(userId, productId);
    }

    @DeleteMapping
    public GetCartResponse clear(@AuthenticationPrincipal Long userId) {

        return cartService.clearCart(userId);
    }
}