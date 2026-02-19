package com.example.finalproject.user.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMyProfileResponse {
    private final String name;
    private final String email;
    private final String phone;
    private final LocalDateTime joinedAt;
}
