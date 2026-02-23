package com.example.finalproject.admin.controller;

import com.example.finalproject.admin.dto.notification.AdminBroadcastCreateRequest;
import com.example.finalproject.admin.dto.notification.AdminBroadcastCreateResponse;
import com.example.finalproject.admin.dto.notification.AdminBroadcastHistoryResponse;
import com.example.finalproject.admin.service.AdminBroadcastService;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications/broadcasts")
public class AdminBroadcastController {

    private final AdminBroadcastService adminBroadcastService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminBroadcastCreateResponse>> createBroadcast(
            Authentication authentication,
            @Valid @RequestBody AdminBroadcastCreateRequest request
    ) {
        AdminBroadcastCreateResponse response =
                adminBroadcastService.createBroadcast(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("알림 발송이 완료되었습니다.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AdminBroadcastHistoryResponse>> getBroadcastHistory(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminBroadcastHistoryResponse response =
                adminBroadcastService.getBroadcastHistory(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("알림 발송 이력 조회가 완료되었습니다.", response));
    }
}
