package com.example.finalproject.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

// 카카오 최초 로그인 후 회원가입 폼 제출 시 사용. 일반 회원가입과 동일한 항목(이름, 이메일, 연락처, 약관). 주소 미수집.
@Getter
@Setter
public class SocialSignupCompleteRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30)
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phone;

    @AssertTrue(message = "필수 약관에 동의해야 합니다.")
    private boolean termsAgreed;

    @AssertTrue(message = "개인정보 처리방침에 동의해야 합니다.")
    private boolean privacyAgreed;

    /** 소셜 추가 가입 시 리다이렉트 URL의 state 쿼리 값(JWT). 세션 대신 JWT로 provider 정보 전달. */
    private String state;
}
