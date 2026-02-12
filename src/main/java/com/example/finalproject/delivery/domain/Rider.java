package com.example.finalproject.delivery.domain;

import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
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
import lombok.*;

/**
 * 라이더 엔티티.
 * <p>
 * 사용자(User)와 1:1 매핑되며, 승인 상태(status)와 운행 상태(operationStatus)를 관리합니다.
 * operationStatus는 @Setter 대신 도메인
 * 메서드(goOnline/goOffline/startDelivering/finishDelivering)로
 * 통제된 상태 전이만 허용합니다.
 * </p>
 * <p>
 * ※ 라이더는 최대 {@value #MAX_CONCURRENT_DELIVERIES}개의 배달을 동시에 진행할 수 있습니다.
 * </p>
 */
@Entity
@Table(name = "riders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rider extends BaseTimeEntity {

    /** 라이더가 동시에 진행할 수 있는 최대 배달 수 */
    public static final int MAX_CONCURRENT_DELIVERIES = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_riders_user"))
    private User user;

    @Column(name = "id_card_verified", nullable = false)
    private Boolean idCardVerified = false;

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

    @Builder
    public Rider(User user, String bankName, String bankAccount, String accountHolder) {
        this.user = user;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
        this.accountHolder = accountHolder;
    }

    public void approve() {
        this.status = RiderApprovalStatus.APPROVED;
    }

    public void reject() {
        this.status = RiderApprovalStatus.REJECTED;
    }

    /**
     * 라이더 영업 시작 (OFFLINE → ONLINE)
     */
    public void goOnline() {
        if (this.operationStatus == RiderOperationStatus.DELIVERING) {
            throw new BusinessException(ErrorCode.RIDER_STATUS_LOCKED_DELIVERING);
        }
        this.operationStatus = RiderOperationStatus.ONLINE;
    }

    /**
     * 라이더 영업 종료 (ONLINE → OFFLINE)
     */
    public void goOffline() {
        if (this.operationStatus == RiderOperationStatus.DELIVERING) {
            throw new BusinessException(ErrorCode.RIDER_STATUS_LOCKED_DELIVERING);
        }
        this.operationStatus = RiderOperationStatus.OFFLINE;
    }

    /**
     * 배달 시작 (ONLINE → DELIVERING)
     */
    public void startDelivering() {
        this.operationStatus = RiderOperationStatus.DELIVERING;
    }

    /**
     * 배달 완료 후 복귀 (DELIVERING → ONLINE)
     */
    public void finishDelivering() {
        this.operationStatus = RiderOperationStatus.ONLINE;
    }
}
