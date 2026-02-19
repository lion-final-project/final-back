package com.example.finalproject.auth.social;

import com.example.finalproject.auth.config.KakaoProperties;
import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.dto.request.SocialSignupCompleteRequest;
import com.example.finalproject.auth.service.KakaoService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoSocialLoginStrategy implements SocialLoginStrategy {

    private final KakaoService kakaoService;
    private final KakaoProperties kakaoProperties;

    @Override
    public String registrationId() {
        return "kakao";
    }

    @Override
    public String frontendSuccessUrl() {
        return kakaoProperties.getFrontendSuccessUrl();
    }

    @Override
    public String resolveProviderUserId(Map<String, Object> attributes) {
        Object idObj = attributes.get("id");
        return idObj != null ? String.valueOf(idObj) : null;
    }

    @Override
    public String resolveNickname(Map<String, Object> attributes) {
        Object props = attributes.get("properties");
        if (props instanceof Map<?, ?> map) {
            Object nickname = map.get("nickname");
            if (nickname != null) {
                return nickname.toString();
            }
        }

        Object kakaoAccount = attributes.get("kakao_account");
        if (kakaoAccount instanceof Map<?, ?> account) {
            Object profile = account.get("profile");
            if (profile instanceof Map<?, ?> map) {
                Object nickname = map.get("nickname");
                if (nickname != null) {
                    return nickname.toString();
                }
            }
        }
        return null;
    }

    @Override
    public boolean isRegistered(String providerUserId) {
        return kakaoService.isKakaoUserRegistered(providerUserId);
    }

    @Override
    public OAuthLoginSessionResult findOrCreate(String providerUserId, String nickname) {
        return kakaoService.findOrCreateByKakaoInfo(providerUserId, nickname);
    }

    @Override
    public OAuthLoginSessionResult completeSignup(String providerUserId, SocialSignupCompleteRequest request) {
        return kakaoService.completeKakaoSignup(providerUserId, request);
    }
}
