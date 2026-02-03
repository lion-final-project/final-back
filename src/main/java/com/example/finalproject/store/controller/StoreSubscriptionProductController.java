package com.example.finalproject.store.controller;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.subscription.dto.request.SubscriptionProductRequest;
import com.example.finalproject.subscription.dto.response.SubscriptionProductResponse;
import com.example.finalproject.subscription.service.SubscriptionProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마트(사장님) 구독 상품 API.
 * API-SOP-009, API-SOP-010 등 마트 전용 구독 상품 CRUD 엔드포인트.
 */
@RestController
@RequestMapping("/api/store/subscription-products")
@RequiredArgsConstructor
public class StoreSubscriptionProductController {

    private final StoreRepository storeRepository;
    private final SubscriptionProductService subscriptionProductService;

    /**
     * API-SOP-010: 구독 상품 등록.
     * 마트가 소유한 구독 상품을 새로 등록한다.
     * storeId: 인증된 사용자(owner)의 마트 ID로 자동 결정. (개발/테스트 시 X-Store-Id 헤더 사용 가능)
     *
     * @param storeIdHeader 개발·테스트용 마트 ID (선택). 없으면 인증된 사용자의 마트 사용
     * @param request       구독 상품 등록 요청 (이름, 가격, 총 배송 횟수, 구성 품목 등)
     * @return 201 Created, 등록된 구독 상품 응답
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionProductResponse>> create(
            @RequestHeader(value = "X-Store-Id", required = false) Long storeIdHeader,
            @Valid @RequestBody SubscriptionProductRequest request) {

        Long storeId = resolveStoreId(storeIdHeader);
        SubscriptionProductResponse response = subscriptionProductService.create(storeId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 요청에 대응하는 마트 ID를 반환한다.
     * 1) X-Store-Id 헤더가 있으면 해당 값 사용 (개발/테스트용).
     * 2) 없으면 인증된 사용자(owner)의 마트 ID 사용.
     *
     * @param storeIdHeader X-Store-Id 헤더 값 (nullable)
     * @return 마트 ID
     * @throws BusinessException 마트를 찾을 수 없을 때 (STORE_NOT_FOUND)
     */
    private Long resolveStoreId(Long storeIdHeader) {
        if (storeIdHeader != null) {
            if (storeRepository.findById(storeIdHeader).isEmpty()) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }
            return storeIdHeader;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        Object principal = auth.getPrincipal();
        Long ownerId = null;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            try {
                ownerId = Long.parseLong(((org.springframework.security.core.userdetails.UserDetails) principal).getUsername());
            } catch (NumberFormatException ignored) {
            }
        } else if (principal instanceof String) {
            try {
                ownerId = Long.parseLong((String) principal);
            } catch (NumberFormatException ignored) {
            }
        }
        if (ownerId == null) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        Store store = storeRepository.findByOwner_Id(ownerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        return store.getId();
    }
}
