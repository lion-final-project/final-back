package com.example.finalproject.settlement.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.order.domain.StoreOrder;
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
@Table(name = "settlement_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDetail extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_settlement_details_settlement"))
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_settlement_details_store_order"))
    private StoreOrder storeOrder;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer fee;

    @Column(name = "net_amount", nullable = false)
    private Integer netAmount;

    @Builder
    public SettlementDetail(Settlement settlement, StoreOrder storeOrder,
                            Integer amount, Integer fee, Integer netAmount) {
        this.settlement = settlement;
        this.storeOrder = storeOrder;
        this.amount = amount;
        this.fee = fee;
        this.netAmount = netAmount;
    }
}
