package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;

public interface RiderRepository extends JpaRepository<Rider, Long> {
    /**
     * 특정 사용자가 이미 라이더로 등록되어 있는지 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 사용자 ID로 라이더 정보 조회
     */
    Optional<Rider> findByUserId(Long userId);

    /**
     *  사용자 이메일로 라이더 정보 조회
     */
    @Query("SELECT r FROM Rider r WHERE r.user.email = :email")
    Optional<Rider> findByUserEmail(String email);

    Page<Rider> findByUser_PhoneContaining(String phone, Pageable pageable);

    Page<Rider> findByStatus(RiderApprovalStatus status, Pageable pageable);

    Page<Rider> findByStatusAndUser_PhoneContaining(RiderApprovalStatus status, String phone, Pageable pageable);

    java.util.List<Rider> findByStatus(RiderApprovalStatus status, Sort sort);

    long countByOperationStatus(RiderOperationStatus operationStatus);

    long countByStatus(RiderApprovalStatus status);

    long countByStatusAndOperationStatus(RiderApprovalStatus status, RiderOperationStatus operationStatus);
}
