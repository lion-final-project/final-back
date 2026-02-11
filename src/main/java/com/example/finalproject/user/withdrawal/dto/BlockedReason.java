package com.example.finalproject.user.withdrawal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BlockedReason {

    private final String code;
    private final String message;
}
