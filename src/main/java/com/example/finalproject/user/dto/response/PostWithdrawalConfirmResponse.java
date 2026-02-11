package com.example.finalproject.user.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostWithdrawalConfirmResponse {

    private final Long userId;
    private final String status;
    private final LocalDateTime deletedAt;
}
