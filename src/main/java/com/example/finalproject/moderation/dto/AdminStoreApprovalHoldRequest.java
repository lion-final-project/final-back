package com.example.finalproject.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStoreApprovalHoldRequest {

    @NotNull
    private Long adminUserId;

    @NotBlank
    private String reason;
}
