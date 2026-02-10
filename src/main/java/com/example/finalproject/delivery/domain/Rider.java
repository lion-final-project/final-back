package com.example.finalproject.delivery.domain;

import com.example.finalproject.delivery.dto.response.RiderResponse;
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
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Setter
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

    public RiderResponse createResponse() {
        return RiderResponse.builder()
                .id(this.getId())
                .userId(this.user.getId())
                .name(this.user.getName())
                .phone(this.user.getPhone())
                .bankAccount(this.bankAccount)
                .bankName(this.bankName)
                .accountHolder(this.accountHolder)
                .status(this.status)
                .operationStatus(this.operationStatus)
                .build();
    }
}
