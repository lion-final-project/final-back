package com.example.finalproject.delivery.repository.custom;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryRepositoryCustom {
  Page<Delivery> findTrackableByUserIdAndStatuses(
    Long userId, PaymentStatus paymentStatus, List<DeliveryStatus> statuses, Pageable pageable
  );
}
