package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.request.PatchRiderStatusRequest;
import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderOperationStatus;

public interface RiderService {
    /**
     * @param username 라이더 아이디
     * @param request 라이더의 상태 변경 요청
     */
    RiderResponse updateOperationStatus(String username, PatchRiderStatusRequest request);
}
