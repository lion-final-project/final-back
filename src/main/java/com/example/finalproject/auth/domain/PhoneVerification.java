package com.example.finalproject.auth.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "phone_verifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_phone_verifications_phone",
                columnNames = "phone"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhoneVerification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 10)
    private String verificationCode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Integer resendCount = 0;

    private LocalDateTime verifiedAt;

    @Column(length = 64)
    private String verificationToken;

    private LocalDateTime consumedAt;

    @Builder
    public PhoneVerification(String phone, String verificationCode, LocalDateTime expiresAt, Integer resendCount) {
        this.phone = phone;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.resendCount = resendCount != null ? resendCount : 0;
    }

    public void updateForResend(String code, LocalDateTime expiresAt) {
        this.verificationCode = code;
        this.expiresAt = expiresAt;
        this.resendCount = this.resendCount + 1;
        this.verifiedAt = null;
        this.verificationToken = null;
        this.consumedAt = null;
    }

    public void markVerified(String token, LocalDateTime verifiedAt) {
        this.verificationToken = token;
        this.verifiedAt = verifiedAt;
    }

    public void markConsumed(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }
}
