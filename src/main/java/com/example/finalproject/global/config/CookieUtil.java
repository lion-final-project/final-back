package com.example.finalproject.global.config;

import org.springframework.http.ResponseCookie;

//JWT 토큰 쿠키 설정
public final class CookieUtil {

    public static final String ACCESS_TOKEN_COOKIE = "nm_accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "nm_refreshToken";
    public static final String COOKIE_PATH = "/";

    private CookieUtil() {
    }

    //쿠키 생성 AT
    public static ResponseCookie createAccessTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false)
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }
    //쿠키 생성 RT
    public static ResponseCookie createRefreshTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(false)
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }

    //쿠키 삭제 AT
    public static ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }

    //쿠키 삭제 RT
    public static ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path(COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
