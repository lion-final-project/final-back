package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.SubscriptionStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionStatusLogRepository extends JpaRepository<SubscriptionStatusLog, Long> {
}
