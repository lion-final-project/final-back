package com.example.finalproject.payment.repository;

import com.example.finalproject.payment.domain.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
}