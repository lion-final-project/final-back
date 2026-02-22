package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingDetailResponse;
import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingItemResponse;
import com.example.finalproject.delivery.dto.response.GetDeliveryHistoryItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerDeliveryTrackingService {
    Page<GetCustomerDeliveryTrackingItemResponse> getMyTrackableDeliveries(String username, Pageable pageable);

    GetCustomerDeliveryTrackingDetailResponse getMyDeliveryTrackingDetail(String username, Long deliveryId);

    /**
     * 배달 이력 조회 (DELIVERED + CANCELLED).
     * 완료되거나 취소된 배달 목록을 최신순으로 반환합니다.
     */
    Page<GetDeliveryHistoryItemResponse> getMyDeliveryHistory(String username, Pageable pageable);
}
