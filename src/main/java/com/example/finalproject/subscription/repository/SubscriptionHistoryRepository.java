package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.SubscriptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, Long> {
}
