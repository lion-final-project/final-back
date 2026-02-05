package com.example.finalproject.moderation.dto.admin.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStoreApprovalRejectRequest {

    @NotNull
    private Long adminUserId;

    @NotBlank
    private String reason;
}

