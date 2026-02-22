package com.example.finalproject.delivery.service;

import com.example.finalproject.delivery.constants.DeliveryRedisKeys;
import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.DeliveryPhoto;
import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingDetailResponse;
import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingItemResponse;
import com.example.finalproject.delivery.dto.response.GetDeliveryHistoryItemResponse;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.delivery.repository.DeliveryPhotoRepository;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.delivery.service.interfaces.CustomerDeliveryTrackingService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerDeliveryTrackingServiceImpl implements CustomerDeliveryTrackingService {

    private static final List<DeliveryStatus> TRACKABLE_STATUSES = List.of(
            DeliveryStatus.REQUESTED,
            DeliveryStatus.ACCEPTED,
            DeliveryStatus.PICKED_UP,
            DeliveryStatus.DELIVERING
    );

    private static final List<DeliveryStatus> HISTORY_STATUSES = List.of(
            DeliveryStatus.DELIVERED,
            DeliveryStatus.CANCELLED
    );

    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryPhotoRepository deliveryPhotoRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Page<GetCustomerDeliveryTrackingItemResponse> getMyTrackableDeliveries(String username, Pageable pageable) {
        User user = findUserByEmail(username);

        Page<Delivery> deliveries = deliveryRepository.findTrackableByUserIdAndStatuses(
                user.getId(),
                PaymentStatus.APPROVED,
                TRACKABLE_STATUSES,
                pageable
        );

        return deliveries.map(this::toTrackingItem);
    }

    @Override
    public Page<GetDeliveryHistoryItemResponse> getMyDeliveryHistory(String username, Pageable pageable) {
        User user = findUserByEmail(username);

        Page<Delivery> deliveries = deliveryRepository.findTrackableByUserIdAndStatuses(
                user.getId(),
                PaymentStatus.APPROVED,
                HISTORY_STATUSES,
                pageable
        );

        return deliveries.map(GetDeliveryHistoryItemResponse::from);
    }

    @Override
    public GetCustomerDeliveryTrackingDetailResponse getMyDeliveryTrackingDetail(String username, Long deliveryId) {
        User user = findUserByEmail(username);
        Delivery delivery = deliveryRepository.findByIdAndStoreOrderOrderUserId(deliveryId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));

        validatePaymentApproved(delivery.getStoreOrder().getOrder().getId());

        String trackingStep = resolveTrackingStep(delivery);
        String trackingStepLabel = resolveTrackingStepLabel(trackingStep);
        LocalDateTime estimatedArrivalAt = calculateEstimatedArrivalAt(delivery);

        GetCustomerDeliveryTrackingDetailResponse.RiderLocation riderLocation = null;
        if (delivery.getStatus() == DeliveryStatus.DELIVERING && delivery.getRider() != null) {
            riderLocation = findRiderLocation(delivery.getRider().getId());
        }

        List<String> deliveryPhotoUrls = List.of();
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            deliveryPhotoUrls = deliveryPhotoRepository.findByDeliveryIdOrderByCreatedAtDesc(delivery.getId())
                    .stream()
                    .map(DeliveryPhoto::getPhotoUrl)
                    .toList();
        }

        return GetCustomerDeliveryTrackingDetailResponse.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getStoreOrder().getOrder().getId())
                .storeOrderId(delivery.getStoreOrder().getId())
                .orderNumber(delivery.getStoreOrder().getOrder().getOrderNumber())
                .storeName(delivery.getStoreOrder().getStore().getStoreName())
                .deliveryAddress(delivery.getStoreOrder().getOrder().getDeliveryAddress())
                .deliveryStatus(delivery.getStatus())
                .trackingStep(trackingStep)
                .trackingStepLabel(trackingStepLabel)
                .estimatedMinutes(delivery.getEstimatedMinutes())
                .estimatedArrivalAt(estimatedArrivalAt)
                .orderReceivedAt(delivery.getStoreOrder().getOrder().getOrderedAt())
                .preparingAt(resolvePreparingAt(delivery))
                .pickupWaitingAt(delivery.getAcceptedAt())
                .deliveringAt(resolveDeliveringAt(delivery))
                .deliveredAt(delivery.getDeliveredAt())
                .riderId(delivery.getRider() != null ? delivery.getRider().getId() : null)
                .riderName(delivery.getRider() != null ? delivery.getRider().getUser().getName() : null)
                .riderPhone(delivery.getRider() != null ? delivery.getRider().getUser().getPhone() : null)
                .riderLocation(riderLocation)
                .deliveryPhotoUrls(deliveryPhotoUrls)
                .build();
    }

    private GetCustomerDeliveryTrackingItemResponse toTrackingItem(Delivery delivery) {
        String trackingStep = resolveTrackingStep(delivery);
        String trackingStepLabel = resolveTrackingStepLabel(trackingStep);

        return GetCustomerDeliveryTrackingItemResponse.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getStoreOrder().getOrder().getId())
                .storeOrderId(delivery.getStoreOrder().getId())
                .orderNumber(delivery.getStoreOrder().getOrder().getOrderNumber())
                .storeName(delivery.getStoreOrder().getStore().getStoreName())
                .deliveryStatus(delivery.getStatus())
                .trackingStep(trackingStep)
                .trackingStepLabel(trackingStepLabel)
                .estimatedMinutes(delivery.getEstimatedMinutes())
                .estimatedArrivalAt(calculateEstimatedArrivalAt(delivery))
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }

    private GetCustomerDeliveryTrackingDetailResponse.RiderLocation findRiderLocation(Long riderId) {
        String riderKey = DeliveryRedisKeys.RIDER_KEY_PREFIX + riderId;
        List<Point> positions = redisTemplate.opsForGeo().position(DeliveryRedisKeys.RIDER_LOC_KEY, riderKey);
        if (positions == null || positions.isEmpty() || positions.get(0) == null) {
            return null;
        }

        Point point = positions.get(0);
        return GetCustomerDeliveryTrackingDetailResponse.RiderLocation.builder()
                .longitude(point.getX())
                .latitude(point.getY())
                .build();
    }

    private String resolveTrackingStep(Delivery delivery) {
        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            return "DELIVERED";
        }
        if (delivery.getStatus() == DeliveryStatus.DELIVERING || delivery.getStatus() == DeliveryStatus.PICKED_UP) {
            return "DELIVERING";
        }
        if (delivery.getStatus() == DeliveryStatus.ACCEPTED) {
            return "PICKUP_WAITING";
        }

        String storeOrderStatus = delivery.getStoreOrder().getStatus().name();
        if ("ACCEPTED".equals(storeOrderStatus) || "READY".equals(storeOrderStatus)) {
            return "PREPARING";
        }
        return "ORDER_RECEIVED";
    }

    private String resolveTrackingStepLabel(String trackingStep) {
        return switch (trackingStep) {
            case "ORDER_RECEIVED" -> "주문 접수";
            case "PREPARING" -> "상품 준비 중";
            case "PICKUP_WAITING" -> "픽업 대기";
            case "DELIVERING" -> "배송 중";
            case "DELIVERED" -> "배송 완료";
            default -> "주문 접수";
        };
    }

    private LocalDateTime calculateEstimatedArrivalAt(Delivery delivery) {
        if (delivery.getEstimatedMinutes() == null) {
            return null;
        }
        if (delivery.getStatus() == DeliveryStatus.DELIVERED || delivery.getStatus() == DeliveryStatus.CANCELLED) {
            return null;
        }
        LocalDateTime baseTime;
        if (delivery.getPickedUpAt() != null) {
            baseTime = delivery.getPickedUpAt();
        } else if (delivery.getAcceptedAt() != null) {
            baseTime = delivery.getAcceptedAt();
        } else {
            baseTime = delivery.getCreatedAt();
        }
        return baseTime.plusMinutes(delivery.getEstimatedMinutes());
    }

    private LocalDateTime resolvePreparingAt(Delivery delivery) {
        if (delivery.getAcceptedAt() != null) {
            return delivery.getAcceptedAt();
        }
        return delivery.getStoreOrder().getOrder().getOrderedAt();
    }

    private LocalDateTime resolveDeliveringAt(Delivery delivery) {
        if (delivery.getStatus() == DeliveryStatus.DELIVERING || delivery.getStatus() == DeliveryStatus.DELIVERED) {
            if (delivery.getPickedUpAt() != null) {
                return delivery.getPickedUpAt();
            }
            return delivery.getStoreOrder().getUpdatedAt();
        }
        return null;
    }

    private void validatePaymentApproved(Long orderId) {
        boolean approved = paymentRepository.findByOrder_Id(orderId)
                .map(payment -> payment.getPaymentStatus() == PaymentStatus.APPROVED)
                .orElse(false);
        if (!approved) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAID);
        }
    }

    private User findUserByEmail(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
