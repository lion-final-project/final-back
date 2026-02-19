package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.component.DeliveryMatchComponent;
import com.example.finalproject.delivery.constants.DeliveryRedisKeys;
import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.DeliveryPhoto;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.response.GetDeliveryDetailResponse;
import com.example.finalproject.delivery.dto.response.GetDeliveryResponse;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.delivery.event.DeliveryStatusChangedEvent;
import com.example.finalproject.delivery.repository.DeliveryPhotoRepository;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.delivery.service.interfaces.DeliveryService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryPhotoRepository deliveryPhotoRepository;
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
        delivery.getStoreOrder().markPickedUp();

        publishStatusChangedEvent(delivery, DeliveryStatus.PICKED_UP);
    }

    /** 배송 시작 (PICKED_UP → DELIVERING) */
    @Override
    @Transactional
    public void startDelivery(String username, Long deliveryId) {
        Delivery delivery = findDeliveryAndValidateRider(username, deliveryId);
        delivery.startDelivering();
        delivery.getStoreOrder().markDelivering();

        publishStatusChangedEvent(delivery, DeliveryStatus.DELIVERING);
    }

    /** 배송 완료 (DELIVERING → DELIVERED) */
    @Override
    @Transactional
    public void completeDelivery(String username, Long deliveryId, String photoUrl) {
        Delivery delivery = findDeliveryAndValidateRider(username, deliveryId);
        delivery.complete();
        delivery.getStoreOrder().markDelivered();

        // 배달 완료 증빙 사진 저장 (사진 URL은 프론트에서 S3 업로드 후 전달)
        DeliveryPhoto photo = DeliveryPhoto.builder()
                .delivery(delivery)
                .photoUrl(photoUrl)
                .expiresAt(LocalDateTime.now().plusDays(90))
                .build();
        deliveryPhotoRepository.save(photo);

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

    /** 내 배달 목록 조회 */
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
        Long storeOwnerId = delivery.getStoreOrder().getStore().getOwner().getId();

        eventPublisher.publishEvent(new DeliveryStatusChangedEvent(
                this, delivery.getId(), newStatus, riderId, customerId, storeOwnerId));
    }
}
