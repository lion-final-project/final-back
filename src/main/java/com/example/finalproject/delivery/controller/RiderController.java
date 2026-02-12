package com.example.finalproject.delivery.controller;

import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderLocationRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.GetDeliveryDetailResponse;
import com.example.finalproject.delivery.dto.response.GetDeliveryResponse;
import com.example.finalproject.delivery.dto.response.GetRiderLocationResponse;
import com.example.finalproject.delivery.dto.response.GetRiderRegistrationStatusResponse;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.delivery.service.interfaces.DeliveryService;
import com.example.finalproject.delivery.service.interfaces.RiderLocationService;
import com.example.finalproject.delivery.service.interfaces.RiderService;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 라이더 API 컨트롤러.
 * <p>
 * 라이더 정보 조회/상태 변경, 등록 신청 관리, 위치 업데이트,
 * 배달 워크플로우(수락/픽업/배송시작/완료) API를 제공합니다.
 * 모든 엔드포인트는 인증(Authentication)이 필요합니다.
 * </p>
 */
@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
@Slf4j
public class RiderController {
    private final RiderService riderService;
    private final RiderLocationService riderLocationService;
    private final DeliveryService deliveryService;

    /**
     * 라이더 정보 조회
     *
     * @param authentication 현재 인증된 사용자 정보
     */
    @GetMapping
    public ResponseEntity<ApiResponse<RiderResponse>> getRiderInfo(Authentication authentication) {
        RiderResponse response = riderService.getRiderInfo(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("라이더 정보 조회가 완료되었습니다.", response));
    }

    /**
     * 라이더의 영업 상태 변경
     *
     * @param request        라이더 영업 상태 변경 요청
     * @param authentication 현재 인증된 사용자 정보
     * @return 변경된 라이더 정보
     */
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<RiderResponse>> updateOperationStatus(
            @Valid @RequestBody PatchRiderStatusRequest request,
            Authentication authentication) {
        RiderResponse response = riderService.updateOperationStatus(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("라이더 상태가 변경되었습니다.", response));
    }

    /**
     * 라이더 등록 신청
     *
     * @param request        라이더 등록 신청 요청
     * @param authentication 현재 인증된 사용자 정보
     * @return 신청된 라이더 정보
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RiderApprovalResponse>> createApproval(
            @Valid @RequestBody PostRiderRegisterRequest request,
            Authentication authentication) {
        RiderApprovalResponse response = riderService.createApproval(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("신청이 완료되었습니다.", response));
    }

    /**
     * 라이더 등록 신청 이력 조회
     *
     * @param pageable       페이지정보
     * @param authentication 현재 인증된 사용자 정보
     */
    @GetMapping("/approvals")
    public ResponseEntity<ApiResponse<Page<RiderApprovalResponse>>> getApprovals(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<RiderApprovalResponse> approvals = riderService.getApprovals(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.success("라이더 신청 목록 조회가 완료되었습니다.", approvals));
    }

    @GetMapping("/registration")
    public ResponseEntity<ApiResponse<GetRiderRegistrationStatusResponse>> getRegistrationStatus(
            Authentication authentication) {
        Optional<GetRiderRegistrationStatusResponse> result =
                riderService.getRegistrationStatus(authentication.getName());
        GetRiderRegistrationStatusResponse response = result.orElseGet(() ->
                GetRiderRegistrationStatusResponse.builder()
                        .status("NONE")
                        .approvalId(null)
                        .build()
        );
        return ResponseEntity.ok(ApiResponse.success("조회했습니다.", response));
    }

    /**
     * 라이더 등록신청 이력 삭제
     *
     * @param approvalsId    신청ID
     * @param authentication 현재 인증된 사용자 정보
     */
    @DeleteMapping("/approvals/{approvals-id}")
    public ResponseEntity<ApiResponse<Void>> deleteApproval(
            @PathVariable("approvals-id") Long approvalsId,
            Authentication authentication) {
        riderService.deleteApproval(authentication.getName(), approvalsId);
        return ResponseEntity.ok(ApiResponse.success("라이더 신청이 삭제되었습니다."));
    }

    // ======================= 위치 API =======================

    /**
     * 라이더 현재 위치 업데이트.
     * <p>
     * Redis GEO에 좌표를 저장하고 주변 배달 목록을 갱신합니다.
     * </p>
     */
    @PostMapping("/locations")
    public ResponseEntity<ApiResponse<Void>> updateRiderLocation(
            @Valid @RequestBody PostRiderLocationRequest request,
            Authentication authentication) {
        riderLocationService.updateRiderLocation(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("라이더 위치가 업데이트되었습니다."));
    }

    /**
     * 특정 라이더 위치 조회
     *
     * @param riderId 레디스 라이더 식별자
     */
    @GetMapping("/locations/{riderId}")
    public ResponseEntity<ApiResponse<GetRiderLocationResponse>> getRiderLocation(
            @PathVariable String riderId,
            Authentication authentication) {
        GetRiderLocationResponse response = riderLocationService.getRiderLocation(riderId);
        return ResponseEntity.ok(ApiResponse.success("라이더 위치 조회가 완료되었습니다.", response));
    }

    /**
     * 라이더 위치 삭제
     *
     * @param riderId 레디스 라이더 식별자
     */
    @DeleteMapping("/locations/{riderId}")
    public ResponseEntity<ApiResponse<Void>> removeRiderLocation(
            @PathVariable String riderId,
            Authentication authentication) {
        riderLocationService.removeRider(riderId);
        return ResponseEntity.ok(ApiResponse.success("라이더 위치가 삭제되었습니다."));
    }

    // ======================= 배달 워크플로우 API =======================
    // 상태 전이: REQUESTED → ACCEPTED → PICKED_UP → DELIVERING → DELIVERED
    // ※ 라이더는 배달 취소 불가 (취소는 관리자/마트 사장만 가능)

    /**
     * 배달 수락 (REQUESTED → ACCEPTED)
     */
    @PostMapping("/deliveries/{deliveryId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptDelivery(
            @PathVariable Long deliveryId,
            Authentication authentication) {
        deliveryService.acceptDelivery(authentication.getName(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success("배달을 수락했습니다."));
    }

    /**
     * 픽업 완료 (ACCEPTED → PICKED_UP)
     */
    @PatchMapping("/deliveries/{deliveryId}/pickup")
    public ResponseEntity<ApiResponse<Void>> pickUpDelivery(
            @PathVariable Long deliveryId,
            Authentication authentication) {
        deliveryService.pickUpDelivery(authentication.getName(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success("픽업이 완료되었습니다."));
    }

    /**
     * 배송 시작 (PICKED_UP → DELIVERING)
     */
    @PatchMapping("/deliveries/{deliveryId}/start")
    public ResponseEntity<ApiResponse<Void>> startDelivery(
            @PathVariable Long deliveryId,
            Authentication authentication) {
        deliveryService.startDelivery(authentication.getName(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success("배송을 시작합니다."));
    }

    /**
     * 배송 완료 (DELIVERING → DELIVERED)
     */
    @PatchMapping("/deliveries/{deliveryId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeDelivery(
            @PathVariable Long deliveryId,
            Authentication authentication) {
        deliveryService.completeDelivery(authentication.getName(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success("배송이 완료되었습니다."));
    }

    /**
     * 내 배달 목록 조회
     */
    @GetMapping("/deliveries")
    public ResponseEntity<ApiResponse<Page<GetDeliveryResponse>>> getMyDeliveries(
            @RequestParam(required = false) DeliveryStatus status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        Page<GetDeliveryResponse> deliveries = deliveryService.getMyDeliveries(
                authentication.getName(), status, pageable);
        return ResponseEntity.ok(ApiResponse.success("배달 목록 조회가 완료되었습니다.", deliveries));
    }

    /**
     * 배달 상세 조회
     */
    @GetMapping("/deliveries/{deliveryId}")
    public ResponseEntity<ApiResponse<GetDeliveryDetailResponse>> getDeliveryDetail(
            @PathVariable Long deliveryId,
            Authentication authentication) {
        GetDeliveryDetailResponse response = deliveryService.getDeliveryDetail(
                authentication.getName(), deliveryId);
        return ResponseEntity.ok(ApiResponse.success("배달 상세 조회가 완료되었습니다.", response));
    }
}
