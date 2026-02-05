package com.example.finalproject.moderation.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;


@Entity
@Table(name = "approvals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Approval extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_approvals_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false)
    private ApplicantType applicantType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by",
            foreignKey = @ForeignKey(name = "fk_approvals_approved_by"))
    private User approvedBy;

    private LocalDateTime approvedAt;

    @Column(name = "held_until")
    private LocalDateTime heldUntil;

    @Builder
    public Approval(User user, ApplicantType applicantType) {
        this.user = user;
        this.applicantType = applicantType;
    }

    public void approve(User admin) {
        this.status = ApprovalStatus.APPROVED;
        this.approvedBy = admin;
        this.approvedAt = LocalDateTime.now();
        this.reason = null;
        this.heldUntil = null;
    }

    public void reject(User admin, String reason) {
        this.status = ApprovalStatus.REJECTED;
        this.approvedBy = admin;
        this.approvedAt = LocalDateTime.now();
        this.reason = StringUtils.hasText(reason) ? reason : null;
        this.heldUntil = null;
    }

    public void hold(User admin, String reason, LocalDateTime heldUntil) {
        this.status = ApprovalStatus.HELD;
        this.approvedBy = admin;
        this.approvedAt = LocalDateTime.now();
        this.reason = StringUtils.hasText(reason) ? reason : null;
        this.heldUntil = heldUntil;
    }
}
