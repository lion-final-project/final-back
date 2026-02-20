package com.example.finalproject.settlement.store.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.settlement.store.enums.SettlementStatus;
import com.example.finalproject.settlement.store.enums.SettlementTargetType;
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

    @Column(name = "settlement_amount", nullable = false)
    private Integer settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account", length = 255)
    private String bankAccount;

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
    }

    public void fail() {
        this.status = SettlementStatus.FAILED;
    }

    public void updateSummary(int totalSales, int platformFee, int pgFee, int settlementAmount) {
        this.totalSales = totalSales;
        this.platformFee = platformFee;
        this.pgFee = pgFee;
        this.settlementAmount = settlementAmount;
    }
}
