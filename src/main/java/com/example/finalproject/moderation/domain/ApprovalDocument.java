package com.example.finalproject.moderation.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.moderation.dto.request.CreateDocumentRequest;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
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
import jakarta.persistence.UniqueConstraint;
import lombok.*;


@Entity
@Table(name = "approval_documents",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_approval_docs_type",
                columnNames = {"approval_id", "document_type"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalDocument extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false)
    private ApplicantType applicantType;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_approval_docs_approval"))
    private Approval approval;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_url", nullable = false, length = 500)
    private String documentUrl;

    @Builder
    public ApprovalDocument(ApplicantType applicantType, Approval approval,
                            DocumentType documentType, String documentUrl) {
        this.applicantType = applicantType;
        this.approval = approval;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
    }

    public static ApprovalDocument create(Approval approval, DocumentType type, String url) {
        return ApprovalDocument.builder()
                .approval(approval)
                .applicantType(approval.getApplicantType()) // Approval에서 가져옴
                .documentType(type)
                .documentUrl(url)
                .build();
    }
}