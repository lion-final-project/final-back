package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.response.RiderResponse;
import com.example.finalproject.delivery.enums.RiderOperationStatus;

public interface RiderService {
    /**
     * @param username 라이더 아이디
     * @param status 라이더의 상태(ONLINE, OFFLINE, DELIVERING)
     */
    RiderResponse updateOperationStatus(String username, RiderOperationStatus status);
}
