package com.example.finalproject.settlement.domain;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rider_settlement_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiderSettlementDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rider_settlement_details_settlement"))
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rider_settlement_details_delivery"))
    private Delivery delivery;

    /** Delivery.riderEarning 스냅샷 */
    @Column(name = "rider_earning", nullable = false)
    private Integer riderEarning;

    /** 해당 배달 건 귀속 RIDER 환불 합계 */
    @Column(name = "refund_amount", nullable = false)
    private Integer refundAmount;

    /** riderEarning - refundAmount (최솟값 0) */
    @Column(name = "net_amount", nullable = false)
    private Integer netAmount;

    @Builder
    public RiderSettlementDetail(Settlement settlement, Delivery delivery,
                                 Integer riderEarning, Integer refundAmount, Integer netAmount) {
        this.settlement = settlement;
        this.delivery = delivery;
        this.riderEarning = riderEarning;
        this.refundAmount = refundAmount;
        this.netAmount = netAmount;
    }
}
