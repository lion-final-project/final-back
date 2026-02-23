package com.example.finalproject.store.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.dto.request.PatchDeliveryAvailableRequest;
import com.example.finalproject.store.dto.request.PatchStoreDescriptionRequest;
import com.example.finalproject.store.dto.request.PatchStoreImageRequest;
import com.example.finalproject.store.dto.request.PostStoreBusinessHourRequest;
import com.example.finalproject.store.dto.request.PostStoreRegistrationRequest;
import com.example.finalproject.store.dto.response.GetMyStoreResponse;
import com.example.finalproject.store.dto.response.GetStoreCategoryResponse;
import com.example.finalproject.store.dto.response.GetStoreRegistrationDetailResponse;
import com.example.finalproject.store.dto.response.GetStoreRegistrationStatusResponse;
import com.example.finalproject.store.dto.response.PostStoreRegistrationResponse;
import com.example.finalproject.store.service.StoreService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/registration")
    public ResponseEntity<ApiResponse<GetStoreRegistrationStatusResponse>> getMyStoreRegistration(
            Authentication authentication
    ) {
        String userName = authentication.getName();
        GetStoreRegistrationStatusResponse data = storeService.getMyStoreRegistration(userName)
                .orElse(GetStoreRegistrationStatusResponse.none());
        return ResponseEntity.ok(ApiResponse.success("조회되었습니다.", data));
    }

    @GetMapping("/registration/detail")
    public ResponseEntity<ApiResponse<GetStoreRegistrationDetailResponse>> getMyStoreRegistrationDetail(
            Authentication authentication
    ) {
        String userName = authentication.getName();
        GetStoreRegistrationDetailResponse data = storeService.getMyStoreRegistrationDetail(userName).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("신청 상세 조회가 완료되었습니다.", data));
    }

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<PostStoreRegistrationResponse>> createStoreApplication(
            @Valid @RequestBody PostStoreRegistrationRequest request,
            Authentication authentication
    ) {
        String userName = authentication.getName();
        PostStoreRegistrationResponse response = storeService.createStoreApplication(userName, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("입점 신청이 완료되었습니다.", response));
    }

    @DeleteMapping("/registration")
    public ResponseEntity<ApiResponse<Void>> cancelStoreRegistration(Authentication authentication) {
        String userName = authentication.getName();
        storeService.cancelStoreRegistration(userName);
        return ResponseEntity.ok(ApiResponse.success("입점 신청이 취소되었습니다."));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<GetStoreCategoryResponse>>> getStoreCategories() {
        List<GetStoreCategoryResponse> list = storeService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("상점 카테고리 목록 조회가 완료되었습니다.", list));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<GetMyStoreResponse>> getMyStore(Authentication authentication) {
        String userName = authentication != null ? authentication.getName() : null;
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return storeService.getMyStoreOptional(userName)
                .map(response -> ResponseEntity.ok(ApiResponse.success("내 상점 정보 조회가 완료되었습니다.", response)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success("내 상점 정보 조회가 완료되었습니다.", null)));
    }

    @GetMapping("/my/business-hours")
    public ResponseEntity<ApiResponse<List<PostStoreBusinessHourRequest>>> getMyStoreBusinessHours(
            Authentication authentication
    ) {
        String userName = authentication.getName();
        List<PostStoreBusinessHourRequest> list = storeService.getStoreBusinessHours(userName);
        return ResponseEntity.ok(ApiResponse.success("영업시간 조회가 완료되었습니다.", list));
    }

    @PutMapping("/my/business-hours")
    public ResponseEntity<ApiResponse<Void>> updateMyStoreBusinessHours(
            Authentication authentication,
            @Valid @RequestBody List<PostStoreBusinessHourRequest> businessHours
    ) {
        String userName = authentication.getName();
        storeService.updateStoreBusinessHours(userName, businessHours);
        return ResponseEntity.ok(ApiResponse.success("영업시간이 수정되었습니다."));
    }

    @PatchMapping("/my/delivery-available")
    public ResponseEntity<ApiResponse<Void>> updateMyDeliveryAvailable(
            Authentication authentication,
            @Valid @RequestBody PatchDeliveryAvailableRequest request
    ) {
        String userName = authentication.getName();
        storeService.updateDeliveryAvailable(userName, request);
        return ResponseEntity.ok(ApiResponse.success(
                Boolean.TRUE.equals(request.getDeliveryAvailable())
                        ? "배달 가능으로 변경되었습니다."
                        : "배달 불가로 변경되었습니다."
        ));
    }

    @PatchMapping("/my/store-image")
    public ResponseEntity<ApiResponse<Void>> updateMyStoreImage(
            Authentication authentication,
            @RequestBody PatchStoreImageRequest request
    ) {
        String userName = authentication.getName();
        storeService.updateStoreImage(userName, request);
        return ResponseEntity.ok(ApiResponse.success("대표 이미지가 수정되었습니다."));
    }

    @PatchMapping("/my/description")
    public ResponseEntity<ApiResponse<Void>> updateMyStoreDescription(
            Authentication authentication,
            @RequestBody PatchStoreDescriptionRequest request
    ) {
        String userName = authentication.getName();
        storeService.updateStoreDescription(userName, request);
        return ResponseEntity.ok(ApiResponse.success("마트 소개가 수정되었습니다."));
    }
}

