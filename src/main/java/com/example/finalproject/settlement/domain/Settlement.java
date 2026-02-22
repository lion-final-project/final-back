package com.example.finalproject.settlement.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private SettlementTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "settlement_period_start", nullable = false)
    private LocalDate settlementPeriodStart;

    @Column(name = "settlement_period_end", nullable = false)
    private LocalDate settlementPeriodEnd;

    @Column(name = "total_sales", nullable = false)
    private Integer totalSales;

    @Column(name = "platform_fee", nullable = false)
    private Integer platformFee;

    @Column(name = "pg_fee", nullable = false, columnDefinition = "integer not null default 0")
    private Integer pgFee = 0;

    @Column(name = "refund_adjustment", nullable = false, columnDefinition = "integer not null default 0")
    private Integer refundAdjustment = 0;

    @Column(name = "settlement_amount", nullable = false)
    private Integer settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account", length = 255)
    private String bankAccount;

    @Column(name = "retry_count", nullable = false, columnDefinition = "integer not null default 0")
    private Integer retryCount = 0;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "last_tried_at")
    private LocalDateTime lastTriedAt;

    private LocalDateTime settledAt;

    @Builder
    public Settlement(SettlementTargetType targetType, Long targetId,
                      LocalDate settlementPeriodStart, LocalDate settlementPeriodEnd,
                      Integer totalSales, Integer platformFee, Integer pgFee,
                      Integer settlementAmount, String bankName, String bankAccount) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.settlementPeriodStart = settlementPeriodStart;
        this.settlementPeriodEnd = settlementPeriodEnd;
        this.totalSales = totalSales;
        this.platformFee = platformFee;
        this.pgFee = pgFee;
        this.settlementAmount = settlementAmount;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }

    public void complete(LocalDateTime settledAt) {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = settledAt != null ? settledAt : LocalDateTime.now();
        this.lastError = null;
    }

    public void fail() {
        this.status = SettlementStatus.FAILED;
        this.retryCount = this.retryCount == null ? 1 : this.retryCount + 1;
        this.lastTriedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        fail();
        this.lastError = errorMessage;
    }

    public void updateSummary(int totalSales, int platformFee, int pgFee, int settlementAmount) {
        this.totalSales = totalSales;
        this.platformFee = platformFee;
        this.pgFee = pgFee;
        this.settlementAmount = settlementAmount;
        this.refundAdjustment = 0;
        this.lastError = null;
        this.lastTriedAt = LocalDateTime.now();
    }

    public void updateSummary(int totalSales, int platformFee, int pgFee, int refundAdjustment, int settlementAmount) {
        this.totalSales = totalSales;
        this.platformFee = platformFee;
        this.pgFee = pgFee;
        this.refundAdjustment = Math.max(0, refundAdjustment);
        this.settlementAmount = settlementAmount;
        this.lastError = null;
        this.lastTriedAt = LocalDateTime.now();
    }

    public boolean isFailed() {
        return this.status == SettlementStatus.FAILED;
    }
}
