package com.example.finalproject.order.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.order.dto.storeorder.request.PatchStoreOrderAcceptRequest;
import com.example.finalproject.order.dto.storeorder.request.PatchStoreOrderRejectRequest;
import com.example.finalproject.order.dto.storeorder.response.GetCompletedStoreOrderResponse;
import com.example.finalproject.order.dto.storeorder.response.GetStoreOrderResponse;
import com.example.finalproject.order.service.StoreOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 마트 사장용 주문 API
 */
@RestController
@RequestMapping("/api/store/orders")
@RequiredArgsConstructor
public class StoreOrderController {

    private final StoreOrderService storeOrderService;

    /**
     * PENDING, ACCEPTED, READY
     * 대시보드 신규 주문 조회
     */
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<List<GetStoreOrderResponse>>> getNewOrders(Authentication authentication) {
        String username = authentication.getName();
        List<GetStoreOrderResponse> list = storeOrderService.getNewOrders(username);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * PICKED_UP, DELIVERING
     * 처리 완료 주문 조회
     */
    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<GetCompletedStoreOrderResponse>>> getCompletedOrders(Authentication authentication) {
        String username = authentication.getName();
        List<GetCompletedStoreOrderResponse> list = storeOrderService.getCompletedOrders(username);
        return ResponseEntity.ok(ApiResponse.success(list));
    }


    /**
     * 주문 내역 확인
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<GetCompletedStoreOrderResponse>>> getOrderHistory(
            Authentication authentication,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String username = authentication.getName();
        Page<GetCompletedStoreOrderResponse> page = storeOrderService.getAllOrders(username, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 주문 접수 (PENDING -> ACCEPTED)
     */
    @PatchMapping("/{storeOrderId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptOrder(
            Authentication authentication,
            @PathVariable Long storeOrderId,
            @RequestBody @Valid PatchStoreOrderAcceptRequest request) {

        storeOrderService.acceptOrder(storeOrderId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("주문 접수 완료"));
    }

    /**
     * 주문 거절 (PENDING -> REJECTED)
     */
    @PatchMapping("/{storeOrderId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectOrder(
            Authentication authentication,
            @RequestBody @Valid PatchStoreOrderRejectRequest request,
            @PathVariable Long storeOrderId) {

        storeOrderService.rejectOrder(storeOrderId, authentication.getName(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("주문 거절 완료"));
    }

    /**
     * 준비 완료 (ACCEPTED -> READY)
     */
    @PatchMapping("/{storeOrderId}/ready")
    public ResponseEntity<ApiResponse<Void>> completePreparation(
            Authentication authentication,
            @PathVariable Long storeOrderId) {

        storeOrderService.completePreparation(storeOrderId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("준비 완료"));
    }
}
