package com.example.finalproject.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 네이버 OAuth2 설정 (동네마켓 코딩 컨벤션: auth 도메인 설정).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "naver")
public class NaverProperties {

    /** OAuth 로그인 성공 후 리다이렉트할 프론트 URL (세션 기반) */
    private String frontendSuccessUrl;
}
