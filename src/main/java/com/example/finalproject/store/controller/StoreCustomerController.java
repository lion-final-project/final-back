package com.example.finalproject.store.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.product.dto.response.GetProductResponse;
import com.example.finalproject.product.enums.ProductSortType;
import com.example.finalproject.product.service.ProductService;
import com.example.finalproject.store.dto.response.GetStoreDetailForCustomerResponse;
import com.example.finalproject.store.service.StoreService;
import com.example.finalproject.subscription.dto.response.GetSubscriptionProductResponse;
import com.example.finalproject.subscription.service.SubscriptionProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고객용 마트 API (API-STO-002 ~ STO-005).
 * 마트 상세, 상품 목록, 리뷰, 구독 상품 목록 등 조회.
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreCustomerController {

    private final StoreService storeService;
    private final SubscriptionProductService subscriptionProductService;
    private final ProductService productService;

    /**
     * 고객용 마트 상세 정보 조회 (가게 정보 탭: 상호명, 사업자번호, 대표자, 주소, 연락처, 소개, 영업시간).
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<GetStoreDetailForCustomerResponse>> getStoreDetail(
            @PathVariable Long storeId) {
        return storeService.getStoreDetailForCustomer(storeId)
                .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 마트 일반 상품 목록 조회 (고객 메뉴 탭용). 삭제되지 않고 판매 중인 상품만 반환.
     *
     * @param storeId 마트 ID
     * @param sort 추천순(RECOMMENDED), 신상품순(NEWEST), 판매량순(SALES), 낮은가격순(PRICE_ASC), 높은가격순(PRICE_DESC)
     * @return 200 OK, 상품 응답 목록
     */
    @GetMapping("/{storeId}/products")
    public ResponseEntity<ApiResponse<List<GetProductResponse>>> listProducts(
            @PathVariable Long storeId,
            @RequestParam(required = false) ProductSortType sort) {

        List<GetProductResponse> list = productService.getProductsByStoreIdForCustomer(storeId, sort);
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
