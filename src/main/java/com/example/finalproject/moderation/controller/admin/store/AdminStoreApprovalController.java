package com.example.finalproject.moderation.controller.admin.store;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.moderation.dto.admin.store.AdminStoreApprovalApproveRequest;
import com.example.finalproject.moderation.dto.admin.store.AdminStoreApprovalDetailResponse;
import com.example.finalproject.moderation.dto.admin.store.AdminStoreApprovalHoldRequest;
import com.example.finalproject.moderation.dto.admin.store.AdminStoreApprovalListResponse;
import com.example.finalproject.moderation.dto.admin.store.AdminStoreApprovalRejectRequest;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.service.admin.store.AdminStoreApprovalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stores/approvals")
@RequiredArgsConstructor
public class AdminStoreApprovalController {

    private final AdminStoreApprovalService adminStoreApprovalService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminStoreApprovalListResponse>>> getApprovals(
            @RequestParam(name = "status", required = false) List<ApprovalStatus> statuses) {
        return ResponseEntity.ok(ApiResponse.success(
                adminStoreApprovalService.getStoreApprovals(statuses)));
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ApiResponse<AdminStoreApprovalDetailResponse>> getApprovalDetail(
            @PathVariable Long approvalId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminStoreApprovalService.getStoreApprovalDetail(approvalId)));
    }

    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long approvalId,
                                     @Valid @RequestBody AdminStoreApprovalApproveRequest request) {
        adminStoreApprovalService.approveStore(approvalId, request.getAdminUserId());
        return ResponseEntity.ok(ApiResponse.success("Approved."));
    }

    @PostMapping("/{approvalId}/hold")
    public ResponseEntity<ApiResponse<Void>> hold(@PathVariable Long approvalId,
                                  @Valid @RequestBody AdminStoreApprovalHoldRequest request) {
        adminStoreApprovalService.holdStore(approvalId, request.getAdminUserId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Held."));
    }

    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long approvalId,
                                    @Valid @RequestBody AdminStoreApprovalRejectRequest request) {
        adminStoreApprovalService.rejectStore(approvalId, request.getAdminUserId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Rejected."));
    }
}

