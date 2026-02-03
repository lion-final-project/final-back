package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.ApprovalDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long> {

    List<ApprovalDocument> findByApprovalId(Long approvalId);
}
