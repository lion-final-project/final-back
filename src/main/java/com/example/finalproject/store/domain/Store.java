package com.example.finalproject.store.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.domain.embedded.StoreAddress;
import com.example.finalproject.store.domain.embedded.SubmittedDocumentInfo;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.enums.StoreStatus;
import com.example.finalproject.user.domain.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    //마트 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stores_category"))
    private StoreCategory storeCategory;

    //상호명
    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    //마트 연락처
    @Column(length = 20)
    private String phone;

    //마트 소개
    @Column(columnDefinition = "TEXT")
    private String description;

    //대표자명
    @Column(name = "representative_name", nullable = false, length = 50)
    private String representativeName;

    //대표자 연락처
    @Column(name = "representative_phone", nullable = false, length = 20)
    private String representativePhone;

    //사용자 제출 서류 정보
    @Embedded
    private SubmittedDocumentInfo submittedDocumentInfo;

    //주소 정보
    @Embedded
    private StoreAddress address;

    //정산 계좌 번호 정보
    @Embedded
    private SettlementAccount settlementAccount;

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

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreBusinessHour> businessHours = new ArrayList<>();

    public void addBusinessHour(StoreBusinessHour businessHour) {
        businessHours.add(businessHour);
        businessHour.assignStore(this);
    }

    // 마트 승인 처리 (상태를 APPROVED로 변경).
    public void approve() {
        this.status = StoreStatus.APPROVED;
    }

    // 마트 거절 처리 (상태를 REJECTED로 변경).
    public void reject() {
        this.status = StoreStatus.REJECTED;
    }

    @Builder
    public Store(User owner, StoreCategory storeCategory, String storeName,
                 String phone, String description,
                 String representativeName, String representativePhone,
                 SubmittedDocumentInfo submittedDocumentInfo,
                 StoreAddress address, SettlementAccount settlementAccount,
                 String storeImage) {
        this.owner = owner;
        this.storeCategory = storeCategory;
        this.storeName = storeName;
        this.phone = phone;
        this.description = description;
        this.representativeName = representativeName;
        this.representativePhone = representativePhone;
        this.submittedDocumentInfo = submittedDocumentInfo;
        this.address = address;
        this.settlementAccount = settlementAccount;
        this.storeImage = storeImage;
    }
}
