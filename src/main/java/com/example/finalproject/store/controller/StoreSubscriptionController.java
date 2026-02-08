package com.example.finalproject.store.controller;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.global.security.CustomUserDetails;
import com.example.finalproject.store.dto.request.AcceptSubscriptionDeliveryRequest;
import com.example.finalproject.store.service.StoreDeliveryScheduleService;
import com.example.finalproject.store.service.StoreSubscriptionDeliveryService;
import com.example.finalproject.subscription.dto.response.GetDeliveryScheduleResponse;
import com.example.finalproject.subscription.service.SubscriptionProductService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마트 구독 배송 일정 API.
 */
@RestController
@RequestMapping("/api/store/subscriptions")
@RequiredArgsConstructor
public class StoreSubscriptionController {

    private final SubscriptionProductService subscriptionProductService;
    private final StoreDeliveryScheduleService storeDeliveryScheduleService;
    private final StoreSubscriptionDeliveryService storeSubscriptionDeliveryService;

    /**
     * 주간 배송 일정 (시간대별) 조회.
     * 3시간 단위: 08:00~11:00, 11:00~14:00, 14:00~17:00, 17:00~20:00
     *
     * @param startDate 주 시작일 (yyyy-MM-dd, 월요일). 미지정 시 이번 주 월요일
     * @return 주간 배송 일정
     */
    @GetMapping("/delivery-schedule")
    public ResponseEntity<ApiResponse<GetDeliveryScheduleResponse>> getDeliverySchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        Long storeId = subscriptionProductService.getStoreIdByUsername(getCurrentUsername());
        GetDeliveryScheduleResponse response = storeDeliveryScheduleService.getDeliverySchedule(storeId, startDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 지정한 날짜·시간대의 구독 배송을 일괄 접수(ACCEPTED) 처리한다.
     * 해당 시간대의 구독 store_orders가 배달 배차 가능 상태로 전환된다.
     */
    @PatchMapping("/delivery-schedule/accept")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> acceptSubscriptionDeliveries(
            @Valid @RequestBody AcceptSubscriptionDeliveryRequest request) {
        Long storeId = subscriptionProductService.getStoreIdByUsername(getCurrentUsername());
        int acceptedCount = storeSubscriptionDeliveryService.acceptSubscriptionDeliveries(
                storeId, request.getDate(), request.getTimeSlot());
        return ResponseEntity.ok(ApiResponse.success(Map.of("acceptedCount", acceptedCount)));
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getUser().getEmail();
        }
        if (principal instanceof String) {
            return (String) principal;
        }
        throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
    }
}
