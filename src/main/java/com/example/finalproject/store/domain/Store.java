package com.example.finalproject.store.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.enums.StoreStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_stores_owner"))
    private User owner;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "business_number", nullable = false, unique = true, length = 12)
    private String businessNumber;

    @Column(name = "representative_name", nullable = false, length = 50)
    private String representativeName;

    @Column(name = "representative_phone", nullable = false, length = 20)
    private String representativePhone;

    @Column(length = 20)
    private String phone;

    @Column(name = "telecom_sales_report_number", length = 50)
    private String telecomSalesReportNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Column(name = "settlement_bank_name", nullable = false, length = 50)
    private String settlementBankName;

    @Column(name = "settlement_bank_account", nullable = false, length = 255)
    private String settlementBankAccount;

    @Column(name = "settlement_account_holder", nullable = false, length = 50)
    private String settlementAccountHolder;

    @Column(name = "store_image", length = 500)
    private String storeImage;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status = StoreStatus.PENDING;

    @Column(name = "is_delivery_available", nullable = false)
    private Boolean isDeliveryAvailable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false)
    private StoreActiveStatus isActive = StoreActiveStatus.ACTIVE;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate = new BigDecimal("5.00");

    private LocalDateTime deletedAt;

    @Builder
    public Store(User owner, String storeName, String businessNumber,
                 String representativeName, String representativePhone,
                 String addressLine1, String postalCode,
                 String settlementBankName, String settlementBankAccount,
                 String settlementAccountHolder) {
        this.owner = owner;
        this.storeName = storeName;
        this.businessNumber = businessNumber;
        this.representativeName = representativeName;
        this.representativePhone = representativePhone;
        this.addressLine1 = addressLine1;
        this.postalCode = postalCode;
        this.settlementBankName = settlementBankName;
        this.settlementBankAccount = settlementBankAccount;
        this.settlementAccountHolder = settlementAccountHolder;
    }
}