package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByApplicantTypeAndStatusIn(ApplicantType applicantType,
                                                  List<ApprovalStatus> statuses);

    Optional<Approval> findByIdAndApplicantType(Long id, ApplicantType applicantType);
}
