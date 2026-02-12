package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.component.DeliveryMatchComponent;
import com.example.finalproject.delivery.constants.DeliveryRedisKeys;
import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.response.GetDeliveryDetailResponse;
import com.example.finalproject.delivery.dto.response.GetDeliveryResponse;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.delivery.event.DeliveryStatusChangedEvent;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.delivery.service.interfaces.DeliveryService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배달 워크플로우 서비스 구현체.
 * <p>
 * 배달 상태 전이(수락→픽업→배송시작→완료)와 조회 기능을 제공합니다.
 * 각 상태 변경 시 {@link DeliveryStatusChangedEvent}를 발행하여
 * 고객에게 SSE 실시간 알림을 전송합니다.
 * </p>
 * <p>
 * ※ 라이더는 최대 {@value Rider#MAX_CONCURRENT_DELIVERIES}건의 배달을 동시 진행할 수 있으며,
 * Redis SET({@code RIDER:DISPATCH:{riderId}})으로 배차 현황을 추적합니다.
 * 라이더는 배달을 취소할 수 없습니다. 취소는 관리자/마트 사장만 가능합니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RiderRepository riderRepository;
    private final DeliveryMatchComponent deliveryMatchComponent;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    /** 배달 수락 (REQUESTED → ACCEPTED) */
    @Override
    @Transactional
    public void acceptDelivery(String username, Long deliveryId) {
        deliveryMatchComponent.acceptDelivery(deliveryId, username);
    }

    /** 픽업 완료 (ACCEPTED → PICKED_UP) */
    @Override
    @Transactional
    public void pickUpDelivery(String username, Long deliveryId) {
        Delivery delivery = findDeliveryAndValidateRider(username, deliveryId);
        delivery.pickUp();

        publishStatusChangedEvent(delivery, DeliveryStatus.PICKED_UP);
    }

    /** 배송 시작 (PICKED_UP → DELIVERING) */
    @Override
    @Transactional
    public void startDelivery(String username, Long deliveryId) {
        Delivery delivery = findDeliveryAndValidateRider(username, deliveryId);
        delivery.startDelivering();

        publishStatusChangedEvent(delivery, DeliveryStatus.DELIVERING);
    }

    /** 배송 완료 (DELIVERING → DELIVERED) */
    @Override
    @Transactional
    public void completeDelivery(String username, Long deliveryId) {
        Delivery delivery = findDeliveryAndValidateRider(username, deliveryId);
        delivery.complete();

        Rider rider = delivery.getRider();

        // Redis 배차 현황 SET에서 완료된 배달 ID 제거
        String dispatchKey = DeliveryRedisKeys.RIDER_DISPATCH_PREFIX + rider.getId();
        redisTemplate.opsForSet().remove(dispatchKey, String.valueOf(deliveryId));

        // 남은 활성 배달이 없으면 라이더 상태를 ONLINE으로 복귀
        Long remaining = redisTemplate.opsForSet().size(dispatchKey);
        if (remaining == null || remaining == 0) {
            rider.finishDelivering();
        }

        publishStatusChangedEvent(delivery, DeliveryStatus.DELIVERED);
    }

    /** 내 배달 목록 조회*/
    @Override
    public Page<GetDeliveryResponse> getMyDeliveries(String username, DeliveryStatus status, Pageable pageable) {
        Rider rider = findRiderByUsername(username);

        Page<Delivery> deliveries;
        if (status != null) {
            deliveries = deliveryRepository.findByRiderAndStatus(rider, status, pageable);
        } else {
            deliveries = deliveryRepository.findByRider(rider, pageable);
        }

        return deliveries.map(GetDeliveryResponse::from);
    }

    /**
     * 배달 상세 조회
     */
    @Override
    public GetDeliveryDetailResponse getDeliveryDetail(String username, Long deliveryId) {
        Delivery delivery = findDeliveryAndValidateRider(username, deliveryId);
        return GetDeliveryDetailResponse.from(delivery);
    }


    /**
     * 배달을 조회하고, 요청한 라이더가 해당 배달에 배정된 라이더인지 검증
     */
    private Delivery findDeliveryAndValidateRider(String username, Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));

        Rider rider = findRiderByUsername(username);

        // 배정된 라이더 본인인지 확인
        if (delivery.getRider() == null || !delivery.getRider().getId().equals(rider.getId())) {
            throw new BusinessException(ErrorCode.DELIVERY_RIDER_NOT_ASSIGNED);
        }

        return delivery;
    }

    private Rider findRiderByUsername(String username) {
        return riderRepository.findByUserEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));
    }

    /**
     * 배달 상태 변경 이벤트를 발행합니다.
     * DeliveryEventListener가 이 이벤트를 수신하여 고객에게 SSE 알림을 전송합니다.
     */
    private void publishStatusChangedEvent(Delivery delivery, DeliveryStatus newStatus) {
        Long customerId = delivery.getStoreOrder().getOrder().getUser().getId();
        Long riderId = delivery.getRider().getId();

        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(
                this, delivery.getId(), newStatus, riderId, customerId));
    }
}
