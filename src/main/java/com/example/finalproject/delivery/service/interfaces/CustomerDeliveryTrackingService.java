package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingDetailResponse;
import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerDeliveryTrackingService {
    Page<GetCustomerDeliveryTrackingItemResponse> getMyTrackableDeliveries(String username, Pageable pageable);

    GetCustomerDeliveryTrackingDetailResponse getMyDeliveryTrackingDetail(String username, Long deliveryId);
}
