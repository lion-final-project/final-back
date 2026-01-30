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

    @NotBlank(message = "은행명은 필수 입력 값입니다.")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수 입력 값입니다.")
    private String bankAccount;

    @NotBlank(message = "예금주명은 필수 입력 값입니다.")
    private String accountHolder;
}
