package com.example.finalproject.delivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostRiderRegisterRequest {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotBlank(message = "연락처는 필수 입력 값입니다.")
    private String phone;

    @NotBlank(message = "은행명은 필수 입력 값입니다.")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수 입력 값입니다.")
    private String bankAccount;

    @NotBlank(message = "예금주명은 필수 입력 값입니다.")
    private String accountHolder;

    @NotBlank(message = "신분증 이미지는 필수 입력 값입니다.")
    private String idCardImage;

    @NotBlank(message = "통장 사본 이미지는 필수 입력 값입니다.")
    private String bankbookImage;
}
