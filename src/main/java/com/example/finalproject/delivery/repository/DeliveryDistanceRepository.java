package com.example.finalproject.delivery.repository;

public interface DeliveryDistanceRepository {
    Double getDistanceToStoreMeter(Long userId, Long storeId);

    /**
     * 지정 배송지 ~ 마트 거리(미터). 배송지/마트 좌표가 없으면 null.
     */
    Double getDistanceToStoreMeterByAddress(Long addressId, Long storeId);
}
