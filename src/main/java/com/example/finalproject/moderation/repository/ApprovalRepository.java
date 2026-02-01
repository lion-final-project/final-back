package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    boolean existsByUserAndApplicantTypeAndStatus(User user, ApplicantType applicantType, ApprovalStatus status);
}
