package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.SubscriptionProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionProductRepository extends JpaRepository<SubscriptionProduct, Long> {
}
