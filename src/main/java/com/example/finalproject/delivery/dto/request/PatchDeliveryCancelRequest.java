package com.example.finalproject.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PatchDeliveryCancelRequest {

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}
