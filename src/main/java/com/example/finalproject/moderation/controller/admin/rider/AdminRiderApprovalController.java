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

    // 배달원 승인 대기 목록 조회 (status 필터)
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminRiderApprovalListResponse>>> getApprovals(
            @RequestParam(name = "status", required = false) List<ApprovalStatus> statuses) {
        return ResponseEntity.ok(ApiResponse.success(
                adminRiderApprovalService.getRiderApprovals(statuses)));
    }

    // 배달원 승인 상세 조회 (approvalId)
    @GetMapping("/{approvalId}")
    public ResponseEntity<ApiResponse<AdminRiderApprovalDetailResponse>> getApprovalDetail(
            @PathVariable Long approvalId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminRiderApprovalService.getRiderApprovalDetail(approvalId)));
    }

    // 승인 처리
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<ApiResponse<Void>> approve(@PathVariable Long approvalId,
                                     @Valid @RequestBody AdminRiderApprovalApproveRequest request) {
        adminRiderApprovalService.approveRider(approvalId, request.getAdminUserId());
        return ResponseEntity.ok(ApiResponse.success("승인 완료"));
    }

    // 보류 처리
    @PostMapping("/{approvalId}/hold")
    public ResponseEntity<ApiResponse<Void>> hold(@PathVariable Long approvalId,
                                  @Valid @RequestBody AdminRiderApprovalHoldRequest request) {
        adminRiderApprovalService.holdRider(approvalId, request.getAdminUserId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("보류 처리 완료"));
    }

    // 거절 처리
    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<ApiResponse<Void>> reject(@PathVariable Long approvalId,
                                    @Valid @RequestBody AdminRiderApprovalRejectRequest request) {
        adminRiderApprovalService.rejectRider(approvalId, request.getAdminUserId(), request.getReason());
        return ResponseEntity.ok(ApiResponse.success("거절 처리 완료"));
    }
}
