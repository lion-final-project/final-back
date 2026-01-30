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
    @Column(name = "applicant_type", nullable = false, columnDefinition = "approval_applicant_type")
    private ApplicantType applicantType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "approval_status DEFAULT 'PENDING'")
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by",
            foreignKey = @ForeignKey(name = "fk_approvals_approved_by"))
    private User approvedBy;

    private LocalDateTime approvedAt;

    @Builder
    public Approval(User user, ApplicantType applicantType) {
        this.user = user;
        this.applicantType = applicantType;
    }
}