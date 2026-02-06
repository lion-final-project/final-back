package com.example.finalproject.moderation.dto.admin.rider;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRiderApprovalHoldRequest {

    @NotBlank
    private String reason;
}

