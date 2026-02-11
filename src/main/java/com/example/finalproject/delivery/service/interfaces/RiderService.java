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

    RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request);

    RiderApprovalResponse createApproval(String username, PostRiderRegisterRequest request);

    Page<RiderApprovalResponse> getApprovals(String username, Pageable pageable);

    Optional<GetRiderRegistrationStatusResponse> getRegistrationStatus(String username);

    void deleteApproval(Long approvalId);

    RiderResponse getRiderInfo(String username);
}