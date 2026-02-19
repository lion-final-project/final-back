package com.example.finalproject.delivery.controller;

import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingDetailResponse;
import com.example.finalproject.delivery.dto.response.GetCustomerDeliveryTrackingItemResponse;
import com.example.finalproject.delivery.service.interfaces.CustomerDeliveryTrackingService;
import com.example.finalproject.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries/tracking")
@RequiredArgsConstructor
public class CustomerDeliveryTrackingController {

    private final CustomerDeliveryTrackingService customerDeliveryTrackingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetCustomerDeliveryTrackingItemResponse>>> getTrackableDeliveries(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        Page<GetCustomerDeliveryTrackingItemResponse> response =
                customerDeliveryTrackingService.getMyTrackableDeliveries(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("배달 추적 목록 조회가 완료되었습니다.", response));
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<ApiResponse<GetCustomerDeliveryTrackingDetailResponse>> getTrackingDetail(
            @PathVariable Long deliveryId,
            Authentication authentication
    ) {
        GetCustomerDeliveryTrackingDetailResponse response =
                customerDeliveryTrackingService.getMyDeliveryTrackingDetail(authentication.getName(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success("배달 추적 상세 조회가 완료되었습니다.", response));
    }
}
