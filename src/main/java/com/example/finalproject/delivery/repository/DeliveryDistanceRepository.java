package com.example.finalproject.delivery.repository;

public interface DeliveryDistanceRepository {
    Double getDistanceToStoreMeter(Long userId, Long storeId);
}
