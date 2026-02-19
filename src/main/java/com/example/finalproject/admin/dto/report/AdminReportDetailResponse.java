package com.example.finalproject.admin.dto.report;

import com.example.finalproject.moderation.enums.ReportStatus;
import com.example.finalproject.moderation.enums.ReportTargetType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminReportDetailResponse {
    private Long reportId;
    private String orderNumber;
    private ReportStatus status;
    private String reasonDetail;
    private String reportResult;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Reporter reporter;
    private Target target;

    @Getter
    @Builder
    public static class Reporter {
        private Long userId;
        private String name;
        private String phone;
        private ReportTargetType type;
    }

    @Getter
    @Builder
    public static class Target {
        private Long userId;
        private String name;
        private String phone;
        private ReportTargetType type;
    }
}

