package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.component.DeliveryMatchComponent;
import com.example.finalproject.delivery.constants.DeliveryRedisKeys;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.request.PostRiderLocationRequest;
import com.example.finalproject.delivery.dto.response.GetRiderLocationResponse;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.delivery.service.interfaces.RiderLocationService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 라이더 위치 관리 서비스 구현체.
 * <p>
 * Redis GEO를 활용하여 라이더의 실시간 위치를 저장/조회/삭제합니다.
 * 기존 RiderServiceImpl에서 SRP(단일 책임 원칙)에 따라 분리되었습니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiderLocationServiceImpl implements RiderLocationService {

    private final StringRedisTemplate redisTemplate;
    private final RiderRepository riderRepository;
    /** 위치 변경 시 주변 배달 목록 갱신을 트리거하는 컴포넌트 */
    private final DeliveryMatchComponent deliveryMatchComponent;

    /**
     * 라이더 위치를 업데이트합니다.
     * <p>
     * 1. 인증된 사용자로 Rider 조회
     * 2. Redis GEO에 좌표 저장
     * 3. 주변 배달 목록 갱신 트리거
     * </p>
     */
    @Override
    public void updateRiderLocation(String username, PostRiderLocationRequest request) {
        Rider rider = riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));

        String memberName = DeliveryRedisKeys.RIDER_KEY_PREFIX + rider.getId();

        log.info("라이더 위치 업데이트 - memberName: {}, lon: {}, lat: {}",
                memberName, request.getLongitude(), request.getLatitude());
        try {
            // Redis GEO에 좌표 저장
            Point point = GeometryUtil.createPointForRedis(request.getLongitude(), request.getLatitude());
            redisTemplate.opsForGeo().add(DeliveryRedisKeys.RIDER_LOC_KEY, point, memberName);

            // 위치 변경 시 주변 배달 목록 SSE 갱신
            deliveryMatchComponent.updateRiderNearbyDeliveries(
                    rider.getId(),
                    request.getLongitude(),
                    request.getLatitude());
        } catch (Exception e) {
            log.error("Redis GEO 저장 실패", e);
            throw e;
        }
    }

    /** Redis GEO에서 라이더 위치 정보를 제거합니다 (영업 종료 시). */
    @Override
    public void removeRider(String riderId) {
        redisTemplate.opsForGeo().remove(DeliveryRedisKeys.RIDER_LOC_KEY, riderId);
    }

    /**
     * Redis GEO에서 라이더의 현재 위치를 조회합니다.
     * 
     * @throws BusinessException RIDER_LOCATION_NOT_FOUND — 위치 정보가 없는 경우
     */
    @Override
    public GetRiderLocationResponse getRiderLocation(String riderId) {
        List<Point> positions = redisTemplate.opsForGeo()
                .position(DeliveryRedisKeys.RIDER_LOC_KEY, riderId);

        if (positions == null || positions.isEmpty() || positions.getFirst() == null) {
            throw new BusinessException(ErrorCode.RIDER_LOCATION_NOT_FOUND);
        }

        Point point = positions.getFirst();
        return GetRiderLocationResponse.builder()
                .riderId(riderId)
                .longitude(point.getX())
                .latitude(point.getY())
                .build();
    }
}
