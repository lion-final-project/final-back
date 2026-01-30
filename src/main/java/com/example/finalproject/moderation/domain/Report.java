package com.example.finalproject.moderation.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.moderation.enums.ReportStatus;
import com.example.finalproject.moderation.enums.ReportTargetType;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id",
            foreignKey = @ForeignKey(name = "fk_reports_store_order"))
    private StoreOrder storeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reports_reporter"))
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporter_type", nullable = false, columnDefinition = "report_target_type")
    private ReportTargetType reporterType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_reports_target"))
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "report_target_type")
    private ReportTargetType targetType;

    @Column(name = "reason_detail", nullable = false, columnDefinition = "TEXT")
    private String reasonDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "report_status DEFAULT 'PENDING'")
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "report_result", columnDefinition = "TEXT")
    private String reportResult;

    private LocalDateTime resolvedAt;

    @Builder
    public Report(StoreOrder storeOrder, User reporter, ReportTargetType reporterType,
                  User target, ReportTargetType targetType, String reasonDetail) {
        this.storeOrder = storeOrder;
        this.reporter = reporter;
        this.reporterType = reporterType;
        this.target = target;
        this.targetType = targetType;
        this.reasonDetail = reasonDetail;
    }
}