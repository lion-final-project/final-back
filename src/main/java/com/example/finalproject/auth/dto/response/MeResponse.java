package com.example.finalproject.auth.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 현재 로그인 사용자 정보 (세션/JWT 공통, /api/auth/me) */
@Getter
@AllArgsConstructor
public class MeResponse {
    private Long userId;
    private String email;
    private String name;
    private List<String> roles;
}
