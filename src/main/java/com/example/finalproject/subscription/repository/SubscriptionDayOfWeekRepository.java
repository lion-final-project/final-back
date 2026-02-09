package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionDayOfWeek;
import com.example.finalproject.subscription.domain.SubscriptionDayOfWeekId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionDayOfWeekRepository extends JpaRepository<SubscriptionDayOfWeek, SubscriptionDayOfWeekId> {

    List<SubscriptionDayOfWeek> findBySubscription(Subscription subscription);
}
