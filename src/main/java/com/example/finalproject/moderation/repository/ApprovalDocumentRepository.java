package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.ApprovalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long> {

}
