package com.example.finalproject.delivery.controller;

import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderLocationRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.GetRiderLocationResponse;
import com.example.finalproject.delivery.dto.response.GetRiderRegistrationStatusResponse;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.service.interfaces.RiderLocationService;
import com.example.finalproject.delivery.service.interfaces.RiderService;
import com.example.finalproject.global.response.ApiResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
@Slf4j
public class RiderController {
    private final RiderService riderService;
    private final RiderLocationService riderLocationService;

    @GetMapping
    public ResponseEntity<ApiResponse<RiderResponse>> getRiderInfo(Authentication authentication) {
        RiderResponse response = riderService.getRiderInfo(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("라이더 정보 조회가 완료되었습니다.", response));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<RiderResponse>> updateOperationStatus(
            @RequestBody PatchRiderStatusRequest request,
            Authentication authentication) {
        RiderResponse response = riderService.updateOperationStatus(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("라이더 상태가 변경되었습니다.", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RiderApprovalResponse>> createApproval(
            @RequestBody PostRiderRegisterRequest request,
            Authentication authentication) {
        RiderApprovalResponse response = riderService.createApproval(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("신청이 완료되었습니다.", response));
    }

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
        return result
                .map(data -> ResponseEntity.ok(ApiResponse.success("조회했습니다.", data)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/approvals/{approvals-id}")
    public ResponseEntity<ApiResponse<Void>> deleteApproval(@PathVariable("approvals-id") Long approvalsId) {
        riderService.deleteApproval(approvalsId);
        return ResponseEntity.ok(ApiResponse.success("라이더 신청을 삭제했습니다."));
    }

    @PostMapping("/locations")
    public ResponseEntity<ApiResponse<Void>> updateRiderLocation(@RequestBody PostRiderLocationRequest request) {
        riderLocationService.updateRiderLocation(request);
        return ResponseEntity.ok(ApiResponse.success("라이더 위치가 업데이트되었습니다."));
    }

    @GetMapping("/locations/{riderId}")
    public ResponseEntity<ApiResponse<GetRiderLocationResponse>> getRiderLocation(@PathVariable String riderId) {
        GetRiderLocationResponse response = riderLocationService.getRiderLocation(riderId);
        return ResponseEntity.ok(ApiResponse.success("라이더 위치 조회가 완료되었습니다.", response));
    }

    @DeleteMapping("/locations/{riderId}")
    public ResponseEntity<ApiResponse<Void>> removeRiderLocation(@PathVariable String riderId) {
        riderLocationService.removeRider(riderId);
        return ResponseEntity.ok(ApiResponse.success("라이더 위치가 삭제되었습니다."));
    }
}