package com.example.finalproject.delivery.component;

import static com.example.finalproject.delivery.constants.DeliveryRedisKeys.*;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.sse.Service.SseService;
import com.example.finalproject.global.sse.enums.SseEventType;
import com.example.finalproject.global.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryMatchComponent {

    private final RedisTemplate<String, String> redisTemplate;
    private final SseService sseService;
    private final RiderRepository riderRepository;
    private final DeliveryRepository deliveryRepository;

    private static final double SEARCH_RADIUS_KM = 10.0;

    /**
     * 1. 신규 배달 발생 시 주변 라이더들에게 알림 (Broadcasting)
     * - 배달 위치를 Redis GEO에 등록한 뒤, 주변 라이더를 검색하여 알림을 전송
     */
    public void notifyNewDelivery(String deliveryId, Double marketLng, Double marketLat) {
        // [이슈 #8] 배달 위치를 Redis GEO에 등록
        Point deliveryPoint = GeometryUtil.createPointForRedis(marketLng, marketLat);

        if (deliveryPoint != null) {
            redisTemplate.opsForGeo().add(
                    DELIVERY_GEO_KEY, deliveryPoint, deliveryId);
        }

        Circle searchArea = GeometryUtil.createSearchCircle(marketLng, marketLat, SEARCH_RADIUS_KM);

        GeoResults<GeoLocation<String>> results = redisTemplate.opsForGeo().search(RIDER_LOC_KEY, searchArea);

        if (results == null)
            return;

        results.getContent().forEach(result -> {
            String riderKey = result.getContent().getName();
            sendToRider(riderKey, SseEventType.NEW_DELIVERY, "새로운 배달 요청: " + deliveryId);
        });
    }

    /**
     * 2. 라이더 이동 시 주변 배달 목록 갱신 (Individual Update)
     */
    public void updateRiderNearbyDeliveries(Long riderId, Double riderLng, Double riderLat) {
        Circle searchArea = GeometryUtil.createSearchCircle(riderLng, riderLat, SEARCH_RADIUS_KM);

        GeoResults<GeoLocation<String>> results = redisTemplate.opsForGeo().search(DELIVERY_GEO_KEY, searchArea);

        List<String> nearbyDeliveryIds = results != null ? results.getContent().stream()
                .map(r -> r.getContent().getName())
                .filter(id -> id != null && id.matches("^\\d+$")) // 숫자 형식만 허용
                .toList() : List.of();

        sendToRider(RIDER_KEY_PREFIX + riderId, SseEventType.NEARBY_DELIVERIES, nearbyDeliveryIds);
    }

    /**
     * 3. 배달 수락 로직 (Distributed Lock & Sync)
     * 여러 라이더 중 가장 먼저 수락한 1명만 성공 처리
     */
    @Transactional
    public void acceptDelivery(Long deliveryId, String username) {
        // 1. Redis 분산 락 선점
        String lockKey = DELIVERY_LOCK_PREFIX + deliveryId;
        Boolean isLocked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, username, Duration.ofSeconds(5));

        if (Boolean.FALSE.equals(isLocked)) {
            throw new BusinessException(ErrorCode.DELIVERY_ALREADY_LOCKED);
        }

        // 트랜잭션 완료 후 락 해제 등록
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                redisTemplate.delete(lockKey);
            }
        });

        // 2. 배달 정보 및 연관된 마트 정보 조회
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));

        if (delivery.getStatus() != DeliveryStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.DELIVERY_ALREADY_MATCHED);
        }

        // 마트의 좌표 추출
        Double marketLng = GeometryUtil.getLongitude(delivery.getStoreLocation());
        Double marketLat = GeometryUtil.getLatitude(delivery.getStoreLocation());

        // 3. 라이더 정보 조회
        Rider rider = riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));

        // 4. 동시 배달 수 제한 확인 (Redis SET 기반)
        String dispatchKey = RIDER_DISPATCH_PREFIX + rider.getId();
        Long currentCount = redisTemplate.opsForSet().size(dispatchKey);
        if (currentCount != null && currentCount >= Rider.MAX_CONCURRENT_DELIVERIES) {
            throw new BusinessException(ErrorCode.RIDER_MAX_DELIVERY_EXCEEDED);
        }

        // 5. 배달 수락 + 라이더 상태 변경
        delivery.accept(rider);
        rider.startDelivering();
        deliveryRepository.save(delivery);

        // 6. Redis 배차 현황에 배달 ID 추가
        redisTemplate.opsForSet().add(dispatchKey, String.valueOf(deliveryId));

        // 7. Redis GEO에서 매칭 완료된 배달 제거 및 실시간 알림 전파
        redisTemplate.opsForGeo().remove(DELIVERY_GEO_KEY, String.valueOf(deliveryId));

        // 주변 라이더들에게 매칭 알림
        broadcastDeliveryMatched(deliveryId, marketLng, marketLat);

        // 상점 사장님에게 배차 완료 알림 전송 (주문 목록 갱신용)
        sseService.send(delivery.getStoreOrder().getStore().getOwner().getId(), SseEventType.DELIVERY_MATCHED,
                deliveryId);
    }

    /**
     * 특정 배달이 매칭되었을 때 주변 라이더들에게 '삭제' 이벤트를 보냅니다.
     */
    private void broadcastDeliveryMatched(Long deliveryId, Double lng, Double lat) {

        Circle area = GeometryUtil.createSearchCircle(lng, lat, SEARCH_RADIUS_KM);

        GeoResults<GeoLocation<String>> results = redisTemplate.opsForGeo().search(RIDER_LOC_KEY,
                area);

        if (results != null) {
            results.getContent().forEach(result -> {
                String riderKey = result.getContent().getName();
                sendToRider(riderKey, SseEventType.DELIVERY_MATCHED, deliveryId);
            });
        }
    }

    /**
     * Rider PK를 추출하여 DB 조회 후 SSE 알림을 전송
     */
    private void sendToRider(String riderKey, SseEventType eventType, Object data) {
        try {
            Long riderId = parseRiderId(riderKey);
            riderRepository.findById(riderId).ifPresent(rider -> {
                // ONLINE + DELIVERING 라이더 모두 알림 수신 (동시 배달 3건까지 가능)
                if (rider.getOperationStatus() == RiderOperationStatus.ONLINE
                        || rider.getOperationStatus() == RiderOperationStatus.DELIVERING) {
                    sseService.send(rider.getUser().getId(), eventType, data);
                }
            });
        } catch (NumberFormatException e) {
            log.warn("유효하지 않은 riderKey 형식: {}", riderKey);
        }
    }

    /**
     * "rider5" → 5L, "rider123" → 123L 형식으로 Rider PK를 추출
     */
    private Long parseRiderId(String riderKey) {
        if (riderKey != null && riderKey.startsWith(RIDER_KEY_PREFIX)) {
            return Long.parseLong(riderKey.substring(RIDER_KEY_PREFIX.length()));
        }
        return Long.parseLong(riderKey);
    }

    /**
     * delivery3 → 3L, delivery123 → 123L 형식으로 Delivery PK를 추출
     *
     */
    private Long parseDeliveryId(String deliveryKey) {
        if (deliveryKey != null && deliveryKey.startsWith(DELIVERY_KEY_PREFIX)) {
            return Long.parseLong(deliveryKey.substring(DELIVERY_KEY_PREFIX.length()));
        }
        return Long.parseLong(deliveryKey);
    }
}