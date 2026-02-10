package com.example.finalproject.admin.store.controller;

import com.example.finalproject.admin.store.dto.AdminStoreDetailResponse;
import com.example.finalproject.admin.store.dto.AdminStoreListResponse;
import com.example.finalproject.admin.store.dto.AdminStoreStatusUpdateRequest;
import com.example.finalproject.admin.store.service.AdminStoreService;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.enums.StoreActiveStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stores")
@RequiredArgsConstructor
public class AdminStoreController {
    private final AdminStoreService adminStoreService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminStoreListResponse>> getStores(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) StoreActiveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        AdminStoreListResponse response = adminStoreService.getStores(name, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("마트 목록 조회 성공", response));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<AdminStoreDetailResponse>> getStoreDetail(@PathVariable Long storeId) {
        AdminStoreDetailResponse response = adminStoreService.getStoreDetail(storeId);
        return ResponseEntity.ok(ApiResponse.success("마트 상세 조회 성공", response));
    }

    @PostMapping("/{storeId}/status")
    public ResponseEntity<ApiResponse<AdminStoreDetailResponse>> updateStoreStatus(
            @PathVariable Long storeId,
            @Valid @RequestBody AdminStoreStatusUpdateRequest request
    ) {
        AdminStoreDetailResponse response = adminStoreService.updateStoreStatus(storeId, request);
        return ResponseEntity.ok(ApiResponse.success("마트 상태 변경 성공", response));
    }
}
