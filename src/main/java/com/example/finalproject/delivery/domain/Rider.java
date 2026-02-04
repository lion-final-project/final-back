package com.example.finalproject.delivery.domain;

import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.global.domain.BaseTimeEntity;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "riders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rider extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_riders_user"))
    private User user;

    @Column(name = "id_card_verified", nullable = false)
    private Boolean idCardVerified = false;

    @Column(name = "id_card_image", length = 500)
    private String idCardImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status", nullable = false)
    private RiderOperationStatus operationStatus = RiderOperationStatus.OFFLINE;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "bank_account", length = 255)
    private String bankAccount;

    @Column(name = "account_holder", length = 50)
    private String accountHolder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiderApprovalStatus status = RiderApprovalStatus.PENDING;

    // 라이더 승인 처리 (상태를 APPROVED로 변경).
    public void approve() {
        this.status = RiderApprovalStatus.APPROVED;
    }

    // 라이더 거절 처리 (상태를 REJECTED로 변경).
    public void reject() {
        this.status = RiderApprovalStatus.REJECTED;
    }

    @Builder
    public Rider(User user) {
        this.user = user;
    }
}
