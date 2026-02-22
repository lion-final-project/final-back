package com.example.finalproject.delivery.repository;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryDistanceRepositoryImpl implements DeliveryDistanceRepository{

    private final EntityManager em;

    @Override
    public Double getDistanceToStoreMeter(Long userId, Long storeId) {

        String sql = "SELECT ST_Distance(a.location::geography, s.location::geography) "
                + "FROM addresses a "
                + "JOIN stores s ON s.id = :storeId "
                + "WHERE a.user_id = :userId "
                + "AND a.is_default = true ";

        try {
            return ((Number) em.createNativeQuery(sql)
                    .setParameter("userId", userId)
                    .setParameter("storeId", storeId)
                    .getSingleResult())
                    .doubleValue();
        } catch (NoResultException e) {
            throw new BusinessException(ErrorCode.DISTANCE_CALCULATION_FAILED);
        }
    }

    @Override
    public Double getDistanceToStoreMeterByAddress(Long addressId, Long storeId) {
        String sql = "SELECT ST_Distance(a.location::geography, s.location::geography) "
                + "FROM addresses a, stores s "
                + "WHERE a.id = :addressId AND s.id = :storeId "
                + "AND a.location IS NOT NULL AND s.location IS NOT NULL ";
        try {
            return ((Number) em.createNativeQuery(sql)
                    .setParameter("addressId", addressId)
                    .setParameter("storeId", storeId)
                    .getSingleResult())
                    .doubleValue();
        } catch (NoResultException e) {
            return null;
        }
    }
}
