package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.response.GetDeliveryDetailResponse;
import com.example.finalproject.delivery.dto.response.GetDeliveryHistoryItemResponse;
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
     * 배송 완료 (DELIVERING → DELIVERED).
     * 프론트엔드에서 S3에 업로드한 증빙 사진 URL을 받아 DeliveryPhoto 레코드를 생성합니다.
     */
    void completeDelivery(String username, Long deliveryId, String photoUrl);

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

    /**
     * 라이더 배달 이력 조회 (완료/취소 건)
     */
    Page<GetDeliveryHistoryItemResponse> getMyDeliveryHistory(String username, Pageable pageable);
}
