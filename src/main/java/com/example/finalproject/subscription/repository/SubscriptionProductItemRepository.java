package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.SubscriptionProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionProductItemRepository extends JpaRepository<SubscriptionProductItem, Long> {
}
