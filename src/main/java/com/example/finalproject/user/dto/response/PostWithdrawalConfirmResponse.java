package com.example.finalproject.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostWithdrawalConfirmResponse {

    private final String message;
    private final boolean loggedOut;
    private final String nextAction;
}
