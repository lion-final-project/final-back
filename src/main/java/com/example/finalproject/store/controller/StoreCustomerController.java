package com.example.finalproject.store.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.dto.response.StoreListItemResponse;
import com.example.finalproject.store.service.StoreService;
import com.example.finalproject.subscription.dto.response.GetSubscriptionProductResponse;
import com.example.finalproject.subscription.service.SubscriptionProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 고객용 마트 API (API-STO-002 ~ STO-005).
 * 마트 상세, 상품 목록, 리뷰, 구독 상품 목록 등 조회.
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreCustomerController {

    private final SubscriptionProductService subscriptionProductService;
    private final StoreService storeService;

    /**
     * 임시: DB에 저장된 전체 상점 목록 조회. (상점 리스트 구현 전 구독 테스트용)
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<StoreListItemResponse>>> listAllStores() {
        List<StoreListItemResponse> list = storeService.getAllStoresForCustomer();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * API-STO-005: 마트 구독 상품 목록 조회.
     * 고객이 마트 상세에서 볼 수 있는 구독 상품 목록을 조회한다.
     * ACTIVE 상태인 구독 상품만 반환된다.
     *
     * @param storeId 마트 ID
     * @return 200 OK, 구독 상품 응답 목록 (마트 없거나 ACTIVE 없으면 빈 배열)
     */
    @GetMapping("/{storeId}/subscription-products")
    public ResponseEntity<ApiResponse<List<GetSubscriptionProductResponse>>> listSubscriptionProducts(
            @PathVariable Long storeId) {
        List<GetSubscriptionProductResponse> list = subscriptionProductService.findListByStoreIdForCustomer(storeId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
