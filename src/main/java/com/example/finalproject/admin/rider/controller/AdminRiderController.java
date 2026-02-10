package com.example.finalproject.admin.rider.controller;

import com.example.finalproject.admin.rider.dto.AdminRiderDetailResponse;
import com.example.finalproject.admin.rider.dto.AdminRiderListResponse;
import com.example.finalproject.admin.rider.dto.AdminRiderStatusUpdateRequest;
import com.example.finalproject.admin.rider.service.AdminRiderService;
import com.example.finalproject.global.response.ApiResponse;
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
@RequestMapping("/api/admin/riders")
@RequiredArgsConstructor
public class AdminRiderController {
    private final AdminRiderService adminRiderService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminRiderListResponse>> getRiders(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        AdminRiderListResponse response = adminRiderService.getRiders(name, phone, page, size);
        return ResponseEntity.ok(ApiResponse.success("라이더 목록 조회 성공", response));
    }

    @GetMapping("/{riderId}")
    public ResponseEntity<ApiResponse<AdminRiderDetailResponse>> getRiderDetail(@PathVariable Long riderId) {
        AdminRiderDetailResponse response = adminRiderService.getRiderDetail(riderId);
        return ResponseEntity.ok(ApiResponse.success("라이더 상세 조회 성공", response));
    }

    @PostMapping("/{riderId}/status")
    public ResponseEntity<ApiResponse<AdminRiderDetailResponse>> updateRiderStatus(
            @PathVariable Long riderId,
            @Valid @RequestBody AdminRiderStatusUpdateRequest request
    ) {
        AdminRiderDetailResponse response = adminRiderService.updateRiderStatus(riderId, request);
        return ResponseEntity.ok(ApiResponse.success("라이더 상태 변경 성공", response));
    }
}
