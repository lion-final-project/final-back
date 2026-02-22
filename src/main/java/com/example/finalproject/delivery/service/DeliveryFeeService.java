package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.repository.DeliveryDistanceRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.product.domain.Product;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryFeeService {
    private static final int DEFAULT_DELIVERY_FEE_WHEN_NO_LOCATION = 3000;

    private final DeliveryDistanceRepository deliveryDistanceRepository;

    public int calculateDeliveryFee(Long userId, Long storeId) {
        Double distanceMeter = deliveryDistanceRepository.getDistanceToStoreMeter(userId, storeId);

        if (distanceMeter == null) {
            throw new BusinessException(ErrorCode.DISTANCE_CALCULATION_FAILED);
        }

        double distanceKm = distanceMeter / 1000.0;

        return determineFeeByDistance(distanceKm);
    }

    /**
     * 지정 배송지 ~ 마트 거리 기준 배송비. (주문/결제창에서 선택한 배송지 사용)
     * 배송지 또는 마트 좌표가 없으면 기본 배송비 반환.
     */
    public int calculateDeliveryFeeByAddress(Long addressId, Long storeId) {
        Double distanceMeter = deliveryDistanceRepository.getDistanceToStoreMeterByAddress(addressId, storeId);

        if (distanceMeter == null) {
            return DEFAULT_DELIVERY_FEE_WHEN_NO_LOCATION;
        }

        double distanceKm = distanceMeter / 1000.0;

        if (distanceKm > 3.0) {
            throw new BusinessException(ErrorCode.DELIVERY_NOT_AVAILABLE);
        }

        return determineFeeByDistance(distanceKm);
    }

    public int calculateTotalDeliveryFee(Long userId, List<Product> products) {
        return products.stream()
                .map(Product::getStore)
                .distinct()
                .mapToInt(store -> calculateDeliveryFee(userId, store.getId()))
                .sum();
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
