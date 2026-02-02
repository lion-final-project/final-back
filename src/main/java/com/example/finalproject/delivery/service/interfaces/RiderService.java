package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiderService {
    /**
     * @param username 유저 이메일
     * @param request 라이더의 상태 변경 요청
     */
    RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request);

    /**
     * 라이더 등록 신청
     * @param username 유저 이메일
     * @param request 라이더 등록 신청 요청
     */
    RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request);

    Page<RiderApprovalResponse> getApprovals(String username, Pageable pageable);

    void deleteApproval(Long approvalId);
}
