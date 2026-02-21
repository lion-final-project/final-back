package com.example.finalproject.communication.repository;

import com.example.finalproject.communication.domain.Inquiry;
import com.example.finalproject.communication.enums.InquiryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findAllByUserId(Long userId, Pageable pageable);

    Optional<Inquiry> findByIdAndUserId(Long id, Long userId);

    Page<Inquiry> findAll(Pageable pageable);

    Page<Inquiry> findAllByStatus(InquiryStatus status, Pageable pageable);

    long countByUserId(Long userId);

    long countByStatus(InquiryStatus status);

    List<Inquiry> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
