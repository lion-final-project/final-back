package com.example.finalproject.auth.dto;

/**
 * 소셜 추가 가입 완료 시 사용하는 state JWT의 클레임.
 * 세션 대신 JWT로 provider 정보를 전달할 때 사용.
 */
public record SocialSignupStateClaims(String registrationId, String providerUserId, String nickname) {
}
