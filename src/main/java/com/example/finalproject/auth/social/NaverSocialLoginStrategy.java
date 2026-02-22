package com.example.finalproject.auth.social;

import com.example.finalproject.auth.config.NaverProperties;
import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.dto.request.SocialSignupCompleteRequest;
import com.example.finalproject.auth.service.NaverService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 네이버 소셜 로그인 전략.
 * 네이버 user info API 응답: { "resultcode": "00", "response": { "id": "...", "nickname": "...", "name": "...", ... } }
 * Spring OAuth2 user-name-attribute: response 이면 attributes에 response 객체가 담기거나, 전체가 담길 수 있음.
 */
@Component
@RequiredArgsConstructor
public class NaverSocialLoginStrategy implements SocialLoginStrategy {

    private final NaverService naverService;
    private final NaverProperties naverProperties;

    @Override
    public String registrationId() {
        return "naver";
    }

    @Override
    public String frontendSuccessUrl() {
        return naverProperties.getFrontendSuccessUrl();
    }

    @Override
    public String resolveProviderUserId(Map<String, Object> attributes) {
        Object response = attributes.get("response");
        if (response instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id != null) return id.toString();
        }
        Object id = attributes.get("id");
        if (id != null) return id.toString();
        return null;
    }

    @Override
    public String resolveNickname(Map<String, Object> attributes) {
        Object response = attributes.get("response");
        if (response instanceof Map<?, ?> map) {
            Object nickname = map.get("nickname");
            if (nickname != null) return nickname.toString();
            Object name = map.get("name");
            if (name != null) return name.toString();
        }
        Object nickname = attributes.get("nickname");
        if (nickname != null) return nickname.toString();
        Object name = attributes.get("name");
        return name != null ? name.toString() : null;
    }

    @Override
    public boolean isRegistered(String providerUserId) {
        return naverService.isNaverUserRegistered(providerUserId);
    }

    @Override
    public OAuthLoginSessionResult findOrCreate(String providerUserId, String nickname) {
        return naverService.findOrCreateByNaverInfo(providerUserId, nickname);
    }

    @Override
    public OAuthLoginSessionResult completeSignup(String providerUserId, SocialSignupCompleteRequest request) {
        return naverService.completeNaverSignup(providerUserId, request);
    }
}
