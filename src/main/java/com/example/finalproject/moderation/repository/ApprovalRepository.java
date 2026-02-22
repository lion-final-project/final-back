package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    boolean existsByUserAndApplicantTypeAndStatus(User user, ApplicantType applicantType, ApprovalStatus status);

    List<Approval> findByApplicantTypeAndStatusIn(ApplicantType applicantType,
                                                  List<ApprovalStatus> statuses);

    Optional<Approval> findByIdAndApplicantType(Long id, ApplicantType applicantType);

    boolean existsByUserAndStatus(User user, ApprovalStatus status);

    Page<Approval> findApprovalsByUserAndApplicantType(User user, ApplicantType applicantType, Pageable pageable);

    Optional<Approval> findFirstByUserAndApplicantTypeAndStatus(User user, ApplicantType applicantType, ApprovalStatus status);

    Optional<Approval> findTopByUserAndApplicantTypeOrderByIdDesc(User user, ApplicantType applicantType);

    long countByApplicantTypeAndStatus(ApplicantType applicantType, ApprovalStatus status);
}
