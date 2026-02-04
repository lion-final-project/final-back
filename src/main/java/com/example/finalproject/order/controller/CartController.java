package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.request.PatchCartUpdateRequest;
import com.example.finalproject.order.dto.request.PostCartAddRequest;
import com.example.finalproject.order.dto.response.GetCartResponse;
import com.example.finalproject.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public GetCartResponse getMyCart(Authentication authentication) {

        return cartService.getMyCart(authentication.getName());
    }

    @PatchMapping("/items/{productId}")
    public GetCartResponse updateQuantity(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody @Valid PatchCartUpdateRequest request) {

        return cartService.updateQuantity(authentication.getName(), productId, request);
    }

    @DeleteMapping("/items/{productId}")
    public GetCartResponse removeItem(
            Authentication authentication,
            @PathVariable Long productId) {

        return cartService.removeItem(authentication.getName(), productId);
    }

    @DeleteMapping
    public GetCartResponse clear(Authentication authentication) {

        return cartService.clearCart(authentication.getName());
    }
}