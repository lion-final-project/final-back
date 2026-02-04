package com.example.finalproject.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendVerificationRequest {

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phone;
}
