package com.example.finalproject.payment.repository;

import com.example.finalproject.payment.domain.Payment;
import java.util.Optional;

import com.example.finalproject.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder_Id(Long orderId);

    long countByOrder_UserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus);
}
