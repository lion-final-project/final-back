package com.example.finalproject.admin.controller.user;

import com.example.finalproject.admin.dto.user.AdminUserDetailResponse;
import com.example.finalproject.admin.dto.user.AdminUserListResponse;
import com.example.finalproject.admin.dto.user.AdminUserStatusUpdateRequest;
import com.example.finalproject.admin.service.user.AdminUserService;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.user.enums.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminUserListResponse>> getUsers(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminUserListResponse response = adminUserService.getUsers(authentication.getName(), keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserDetail(
            Authentication authentication,
            @PathVariable Long userId
    ) {
        AdminUserDetailResponse response = adminUserService.getUserDetail(authentication.getName(), userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> updateUserStatus(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request
    ) {
        AdminUserDetailResponse response =
                adminUserService.updateUserStatus(authentication.getName(), userId, request);
        return ResponseEntity.ok(ApiResponse.success("회원 상태가 변경되었습니다.", response));
    }
}
