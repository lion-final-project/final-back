package com.example.finalproject.delivery.controller;

import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.delivery.service.interfaces.RiderService;
import com.example.finalproject.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
public class RiderController {
    private final RiderService riderService;

    /**
     * 라이더의 영업 상태 변경
     * @param request  라이더의 영업 상태 변경 요청
     * @param userDetails 현재 인증된 사용자 정보
     * @return 변경된 라이더 정보
     */
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<RiderResponse>> updateOperationStatus(
            @RequestBody PatchRiderStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        RiderResponse response = riderService.updateOperationStatus(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("라이더 상태가 변경되었습니다.", response));
    }


    /**
     * 라이더 등록 신청
     * @param request 라이더 등록 신청 요청
     * @param userDetails 현재 인증된 사용자 정보
     * @return 신청된 라이더 정보
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RiderApprovalResponse>> createApproval(
            @RequestBody PostRiderRegisterRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        RiderApprovalResponse response = riderService.createApproval(userDetails.getUsername(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("신청이 완료되었습니다.", response));
    }
}
