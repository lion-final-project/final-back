package com.example.finalproject.auth.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 현재 로그인 사용자 기본 정보 (프로필 조회, /api/auth/me). 이메일은 읽기 전용. */
@Getter
@AllArgsConstructor
public class MeResponse {
    private Long userId;
    private String email;   // 읽기 전용 (수정 API 없음)
    private String name;
    private String phone;  // 연락처
    private LocalDateTime joinedAt; // 가입일
    private List<String> roles;
}
