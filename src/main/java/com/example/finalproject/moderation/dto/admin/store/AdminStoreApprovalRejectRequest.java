package com.example.finalproject.moderation.dto.admin.store;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStoreApprovalRejectRequest {

    @NotBlank
    private String reason;
}

