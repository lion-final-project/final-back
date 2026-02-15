package com.example.finalproject.moderation.domain;


import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.user.domain.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @OneToMany(mappedBy = "approval", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ApprovalDocument> documents = new ArrayList<>();

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

    public void addDocument(DocumentType type, String url) {
        if (type == null || !StringUtils.hasText(url)) {
            return;
        }
        ApprovalDocument document = ApprovalDocument.builder()
                .applicantType(this.applicantType)
                .approval(this)
                .documentType(type)
                .documentUrl(url)
                .build();
        this.documents.add(document);
    }

    public RiderApprovalResponse createResponse(Rider rider) {
        List<String> docUrls = new ArrayList<>();
        for (ApprovalDocument document : documents) {
            docUrls.add(document.getDocumentUrl());
        }
        return RiderApprovalResponse.builder()
                .approvalId(this.id)
                .userId(this.user.getId())
                .name(rider.getDisplayName())
                .phone(rider.getDisplayPhone())
                .bankName(rider.getBankName())
                .bankAccount(rider.getBankAccount())
                .accountHolder(rider.getAccountHolder())
                .documents(docUrls)
                .status(this.status.name())
                .build();
    }
}
