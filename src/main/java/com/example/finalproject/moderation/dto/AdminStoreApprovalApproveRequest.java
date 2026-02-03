package com.example.finalproject.moderation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStoreApprovalApproveRequest {

    @NotNull
    private Long adminUserId;
}
