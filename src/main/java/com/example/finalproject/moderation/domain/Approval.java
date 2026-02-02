package com.example.finalproject.moderation.domain;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.moderation.dto.request.CreateApprovalRequest;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.user.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_approvals_user"))
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
    @JoinColumn(name = "approved_by", foreignKey = @ForeignKey(name = "fk_approvals_approved_by"))
    private User approvedBy;

    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "approval", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApprovalDocument> documents = new ArrayList<>();

    public void addDocument(DocumentType type, String url) {
        ApprovalDocument doc = ApprovalDocument.create(this, type, url);
        documents.add(doc);
        doc.setApproval(this);
    }

    @Builder
    public Approval(User user, ApplicantType applicantType) {
        this.user = user;
        this.applicantType = applicantType;
    }

    public RiderApprovalResponse createResponse(Rider rider) {
        return RiderApprovalResponse.builder()
                .approvalId(this.getId())
                .userId(this.user.getId())
                .name(this.user.getName())
                .phone(this.user.getPhone())
                .bankName(rider.getBankName())
                .bankAccount(rider.getBankAccount())
                .accountHolder(rider.getAccountHolder())
                .status(status.name())
                .documents(documents.stream()
                        .map(ApprovalDocument::getDocumentUrl)
                        .toList())
                .build();
    }
}