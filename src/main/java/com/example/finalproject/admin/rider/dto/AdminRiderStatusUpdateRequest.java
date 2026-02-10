package com.example.finalproject.admin.rider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminRiderStatusUpdateRequest {
    @NotNull(message = "isActive는 필수입니다.")
    private Boolean isActive;

    private String reason;
}
