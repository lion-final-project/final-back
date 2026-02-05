package com.example.finalproject.payment.repository;

import com.example.finalproject.payment.domain.PaymentMethod;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findFirstByUserIdAndIsDefaultTrue(Long userId);
}
