package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.domain.SubscriptionProductDayOfWeek;
import com.example.finalproject.subscription.domain.SubscriptionProductDayOfWeekId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionProductDayOfWeekRepository extends JpaRepository<SubscriptionProductDayOfWeek, SubscriptionProductDayOfWeekId> {

    List<SubscriptionProductDayOfWeek> findBySubscriptionProductOrderById_DayOfWeekAsc(SubscriptionProduct subscriptionProduct);

    void deleteBySubscriptionProduct(SubscriptionProduct subscriptionProduct);
}
