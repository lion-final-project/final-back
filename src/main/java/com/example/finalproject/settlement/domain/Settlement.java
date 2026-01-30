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
    @Column(name = "target_type", nullable = false, columnDefinition = "settlement_target_type")
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

    @Column(name = "settlement_amount", nullable = false)
    private Integer settlementAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "settlement_status DEFAULT 'PENDING'")
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account", length = 255)
    private String bankAccount;

    private LocalDateTime settledAt;

    @Builder
    public Settlement(SettlementTargetType targetType, Long targetId,
                      LocalDate settlementPeriodStart, LocalDate settlementPeriodEnd,
                      Integer totalSales, Integer platformFee,
                      Integer settlementAmount) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.settlementPeriodStart = settlementPeriodStart;
        this.settlementPeriodEnd = settlementPeriodEnd;
        this.totalSales = totalSales;
        this.platformFee = platformFee;
        this.settlementAmount = settlementAmount;
    }
}