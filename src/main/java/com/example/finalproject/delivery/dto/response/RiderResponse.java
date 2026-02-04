package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.domain.Rider;


//컴파일용은 가능해야해서 억지로 만들었습니다...
public class RiderResponse {


    public static RiderResponse from(Rider rider) {
        RiderResponse riderResponse = new RiderResponse();
        return riderResponse;
    }

}
