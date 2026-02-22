package com.example.finalproject.admin.controller.report;

import com.example.finalproject.admin.dto.report.AdminReportDetailResponse;
import com.example.finalproject.admin.dto.report.AdminReportListResponse;
import com.example.finalproject.admin.dto.report.AdminReportResolveRequest;
import com.example.finalproject.admin.service.report.AdminReportService;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.moderation.enums.ReportStatus;
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
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminReportListResponse>> getReports(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminReportListResponse response =
                adminReportService.getReports(authentication.getName(), keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("신고 목록 조회가 완료되었습니다.", response));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> getReportDetail(
            Authentication authentication,
            @PathVariable Long reportId
    ) {
        AdminReportDetailResponse response =
                adminReportService.getReportDetail(authentication.getName(), reportId);
        return ResponseEntity.ok(ApiResponse.success("신고 상세 조회가 완료되었습니다.", response));
    }

    @PatchMapping("/{reportId}/resolve")
    public ResponseEntity<ApiResponse<AdminReportDetailResponse>> resolveReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @Valid @RequestBody AdminReportResolveRequest request
    ) {
        AdminReportDetailResponse response =
                adminReportService.resolveReport(authentication.getName(), reportId, request.getResultMessage());
        return ResponseEntity.ok(ApiResponse.success("신고 처리가 완료되었습니다.", response));
    }
}

