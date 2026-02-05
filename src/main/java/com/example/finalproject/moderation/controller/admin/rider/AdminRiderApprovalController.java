package com.example.finalproject.moderation.controller.admin.rider;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.moderation.dto.admin.rider.AdminRiderApprovalApproveRequest;
import com.example.finalproject.moderation.dto.admin.rider.AdminRiderApprovalDetailResponse;
import com.example.finalproject.moderation.dto.admin.rider.AdminRiderApprovalHoldRequest;
import com.example.finalproject.moderation.dto.admin.rider.AdminRiderApprovalListResponse;
import com.example.finalproject.moderation.dto.admin.rider.AdminRiderApprovalRejectRequest;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.service.admin.rider.AdminRiderApprovalService;
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
@RequestMapping("/api/admin/riders/approvals")
@RequiredArgsConstructor
public class AdminRiderApprovalController {

    private final AdminRiderApprovalService adminRiderApprovalService;

    // ?쇱씠???뱀씤 ?湲?蹂대쪟 紐⑸줉 議고쉶 (status 由ъ뒪?몃줈 ?꾪꽣).
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminRiderApprovalListResponse>>> getApprovals(
            @RequestParam(name = "status", required = false) List<ApprovalStatus> statuses) {
        return ResponseEntity.ok(ApiResponse.success(
                adminRiderApprovalService.getRiderApprovals(statuses)));
    }

    // ?쇱씠???뱀씤 ?곸꽭 議고쉶 (approvalId).
    @GetMapping("/{approvalId}")
    public ResponseEntity<ApiResponse<AdminRiderApprovalDetailResponse>> getApprovalDetail(
            @PathVariable Long approvalId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminRiderApprovalService.getRiderApprovalDetail(approvalId)));
    }

    // ?뱀씤 泥섎━ (approvalId, adminUserId).
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long approvalId,
                                     @Valid @RequestBody AdminRiderApprovalApproveRequest request) {
        adminRiderApprovalService.approveRider(approvalId, request.getAdminUserId());
        return ResponseEntity.ok(ApiResponse.success("Approved."));
    }

    // 蹂대쪟 泥섎━ (approvalId, adminUserId, reason).
    @PostMapping("/{approvalId}/hold")
    public ResponseEntity<ApiResponse<Void>> hold(@PathVariable Long approvalId,
                                  @Valid @RequestBody AdminRiderApprovalHoldRequest request) {
        adminRiderApprovalService.holdRider(approvalId, request.getAdminUserId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Held."));
    }

    // 嫄곗젅 泥섎━ (approvalId, adminUserId, reason).
    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long approvalId,
                                    @Valid @RequestBody AdminRiderApprovalRejectRequest request) {
        adminRiderApprovalService.rejectRider(approvalId, request.getAdminUserId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Rejected."));
    }
}

