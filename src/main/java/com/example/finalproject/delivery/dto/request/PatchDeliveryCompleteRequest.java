package com.example.finalproject.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PatchDeliveryCompleteRequest {

    @NotBlank(message = "배달 완료 증빙 사진 URL은 필수입니다.")
    private String photoUrl;
}
