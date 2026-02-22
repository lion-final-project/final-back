package com.example.finalproject.admin.service.report;

import com.example.finalproject.admin.dto.report.AdminReportDetailResponse;
import com.example.finalproject.admin.dto.report.AdminReportListItemResponse;
import com.example.finalproject.admin.dto.report.AdminReportListResponse;
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.service.NotificationService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.moderation.domain.Report;
import com.example.finalproject.moderation.enums.ReportStatus;
import com.example.finalproject.moderation.repository.ReportRepository;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public AdminReportListResponse getReports(String adminEmail, String keyword, ReportStatus status, Pageable pageable) {
        validateAdmin(adminEmail);

        Page<Report> reportPage = reportRepository.searchForAdmin(keyword, status, pageable);

        List<AdminReportListItemResponse> content = reportPage.getContent().stream()
                .map(this::toListItem)
                .toList();

        AdminReportListResponse.Stats stats = AdminReportListResponse.Stats.builder()
                .total(reportRepository.count())
                .pending(reportRepository.countByStatus(ReportStatus.PENDING))
                .resolved(reportRepository.countByStatus(ReportStatus.RESOLVED))
                .build();

        AdminReportListResponse.PageInfo pageInfo = AdminReportListResponse.PageInfo.builder()
                .page(reportPage.getNumber())
                .size(reportPage.getSize())
                .totalElements(reportPage.getTotalElements())
                .totalPages(reportPage.getTotalPages())
                .hasNext(reportPage.hasNext())
                .build();

        return AdminReportListResponse.builder()
                .content(content)
                .stats(stats)
                .page(pageInfo)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminReportDetailResponse getReportDetail(String adminEmail, Long reportId) {
        validateAdmin(adminEmail);
        Report report = findReport(reportId);
        return toDetail(report);
    }

    @Transactional
    public AdminReportDetailResponse resolveReport(String adminEmail, Long reportId, String resultMessage) {
        validateAdmin(adminEmail);

        Report report = findReport(reportId);
        if (report.getStatus() == ReportStatus.RESOLVED) {
            return toDetail(report);
        }

        report.resolve(resultMessage);
        notifyResolution(report, resultMessage);

        return toDetail(report);
    }

    private void notifyResolution(Report report, String resultMessage) {
        String orderNumber = extractOrderNumber(report);
        String title = "[신고 처리 완료] 주문 " + orderNumber;
        String content = "신고 처리 결과: " + resultMessage;

        notificationService.createNotification(
                report.getReporter().getId(), title, content, NotificationRefType.CUSTOMER
        );

        if (!report.getReporter().getId().equals(report.getTarget().getId())) {
            notificationService.createNotification(
                    report.getTarget().getId(), title, content, NotificationRefType.CUSTOMER
            );
        }
    }

    private AdminReportListItemResponse toListItem(Report report) {
        return AdminReportListItemResponse.builder()
                .reportId(report.getId())
                .orderNumber(extractOrderNumber(report))
                .status(report.getStatus())
                .reasonDetail(report.getReasonDetail())
                .createdAt(report.getCreatedAt())
                .reporter(AdminReportListItemResponse.Reporter.builder()
                        .userId(report.getReporter().getId())
                        .name(report.getReporter().getName())
                        .phone(report.getReporter().getPhone())
                        .type(report.getReporterType())
                        .build())
                .target(AdminReportListItemResponse.Target.builder()
                        .userId(report.getTarget().getId())
                        .name(report.getTarget().getName())
                        .phone(report.getTarget().getPhone())
                        .type(report.getTargetType())
                        .build())
                .build();
    }

    private AdminReportDetailResponse toDetail(Report report) {
        return AdminReportDetailResponse.builder()
                .reportId(report.getId())
                .orderNumber(extractOrderNumber(report))
                .status(report.getStatus())
                .reasonDetail(report.getReasonDetail())
                .reportResult(report.getReportResult())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .reporter(AdminReportDetailResponse.Reporter.builder()
                        .userId(report.getReporter().getId())
                        .name(report.getReporter().getName())
                        .phone(report.getReporter().getPhone())
                        .type(report.getReporterType())
                        .build())
                .target(AdminReportDetailResponse.Target.builder()
                        .userId(report.getTarget().getId())
                        .name(report.getTarget().getName())
                        .phone(report.getTarget().getPhone())
                        .type(report.getTargetType())
                        .build())
                .build();
    }

    private String extractOrderNumber(Report report) {
        StoreOrder storeOrder = report.getStoreOrder();
        if (storeOrder == null || storeOrder.getOrder() == null) {
            return "-";
        }
        return storeOrder.getOrder().getOrderNumber();
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "신고 내역을 찾을 수 없습니다."));
    }

    private User validateAdmin(String adminEmail) {
        User admin = userRepository.findByEmailAndDeletedAtIsNull(adminEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED));
        if (!admin.isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED);
        }
        return admin;
    }
}

