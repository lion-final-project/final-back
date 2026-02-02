package com.example.finalproject.moderation.repository;

import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    boolean existsByUserAndStatus(User user, ApprovalStatus status);
}
