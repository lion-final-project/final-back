package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.response.GetDeliveryDetailResponse;
import com.example.finalproject.delivery.dto.response.GetDeliveryResponse;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 배달 워크플로우 서비스 인터페이스.
 * <p>
 * 라이더가 배달을 수락/픽업/배송시작/완료하는 상태 전이와
 * 배달 목록/상세 조회 기능을 정의합니다.
 * </p>
 * <p>
 * ※ 라이더는 배달을 취소할 수 없습니다.
 * </p>
 */
public interface DeliveryService {

    /**
     * 배달 수락 (REQUESTED → ACCEPTED)
     */
    void acceptDelivery(String username, Long deliveryId);

    /**
     * 픽업 완료 (ACCEPTED → PICKED_UP)
     */
    void pickUpDelivery(String username, Long deliveryId);

    /**
     * 배송 시작 (PICKED_UP → DELIVERING)
     */
    void startDelivery(String username, Long deliveryId);

    /**
     * 배송 완료 (DELIVERING → DELIVERED)
     */
    void completeDelivery(String username, Long deliveryId);

    /**
     * 내 배달 목록 조회.
     * 
     * @param status null이면 전체, 값이 있으면 해당 상태만 필터링
     */
    Page<GetDeliveryResponse> getMyDeliveries(String username, DeliveryStatus status, Pageable pageable);

    /**
     * 배달 상세 조회
     */
    GetDeliveryDetailResponse getDeliveryDetail(String username, Long deliveryId);
}
