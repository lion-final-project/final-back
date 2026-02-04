package com.example.finalproject.auth.repository;

import com.example.finalproject.auth.domain.PhoneVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {
    Optional<PhoneVerification> findByPhone(String phone);
}
