package com.example.finalproject.delivery.service.interfaces;

import com.example.finalproject.delivery.dto.request.PostRiderLocationRequest;
import com.example.finalproject.delivery.dto.response.GetRiderLocationResponse;

public interface RiderLocationService {
    void updateRiderLocation(PostRiderLocationRequest request);

    void removeRider(String riderId);

    GetRiderLocationResponse getRiderLocation(String riderId);
}