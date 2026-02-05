package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    boolean existsByUserAndApplicantTypeAndStatus(User user, ApplicantType applicantType, ApprovalStatus status);

    boolean existsByUserAndStatus(User user, ApprovalStatus status);

    Page<Approval> findApprovalsByUserAndApplicantType(User user, ApplicantType applicantType, Pageable pageable);

    Optional<Approval> findFirstByUserAndApplicantTypeAndStatus(User user, ApplicantType applicantType, ApprovalStatus status);
}
