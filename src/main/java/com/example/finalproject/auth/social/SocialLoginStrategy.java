package com.example.finalproject.auth.social;

import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.dto.request.SocialSignupCompleteRequest;
import java.util.Map;

public interface SocialLoginStrategy {

    String registrationId();

    String frontendSuccessUrl();

    String resolveProviderUserId(Map<String, Object> attributes);

    String resolveNickname(Map<String, Object> attributes);

    boolean isRegistered(String providerUserId);

    OAuthLoginSessionResult findOrCreate(String providerUserId, String nickname);

    OAuthLoginSessionResult completeSignup(String providerUserId, SocialSignupCompleteRequest request);
}
