package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.request.PostRiderRegisterRequest;
import com.example.finalproject.delivery.dto.response.GetRiderRegistrationStatusResponse;
import com.example.finalproject.delivery.dto.response.RiderApprovalResponse;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiderService {

    /**
     * @param username 유저 이메일
     * @param request  라이더 영업 상태 변경 요청
     * @return 변경된 라이더 정보
     */
    RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request);

    /**
     * 라이더 등록 신청
     * 
     * @param username 유저 이메일
     * @param request  라이더 등록 신청 요청
     * @return 등록 신청된 라이더정보
     */
    RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request);

    /**
     * 라이더 신청 목록 조회
     * 
     * @param username 유저 이메일
     * @param pageable 페이징 정보
     * @return 라이더 등록 신청 목록
     */
    Page<RiderApprovalResponse> getApprovals(String username, Pageable pageable);

    /**
     * 라이더 등록 신청 상태 조회
     * @param username 유저 이메일
     * @return 최신 신청 상태
     */
    Optional<GetRiderRegistrationStatusResponse> getRegistrationStatus(String username);

    /**
     * 라이더 신청 삭제
     * 
     * @param username   유저 이메일
     * @param approvalId 신청ID
     */
    void deleteApproval(String username, Long approvalId);

    /**
     * 라이더 정보조회
     * 
     * @param username 유저 이메일
     */
    RiderResponse getRiderInfo(String username);
}
