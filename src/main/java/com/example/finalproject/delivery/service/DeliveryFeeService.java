package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.repository.DeliveryDistanceRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryFeeService {
    private final DeliveryDistanceRepository deliveryDistanceRepository;

    public int calculateDeliveryFee(Long userId, Long storeId) {
        Double distanceMeter = deliveryDistanceRepository.getDistanceToStoreMeter(userId, storeId);

        if (distanceMeter == null) {
            throw new BusinessException(ErrorCode.DISTANCE_CALCULATION_FAILED);
        }

        double distanceKm = distanceMeter / 1000.0;

        return determineFeeByDistance(distanceKm);
    }

    private int determineFeeByDistance(double km) {
        if (km > 3.0) {
            throw new BusinessException(ErrorCode.DELIVERY_NOT_AVAILABLE);
        }

        if (km <= 1.0) {
            return 3000;
        }
        if (km <= 2.0) {
            return 4000;
        }
        return 5000; // 마지막 구간은 2.0 < km <= 3.0
    }
}
