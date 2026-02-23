package com.example.finalproject.auth.config;

import com.example.finalproject.auth.dto.response.LoginResponse;
import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.service.AuthService;
import com.example.finalproject.auth.social.SocialLoginStrategy;
import com.example.finalproject.auth.social.SocialLoginStrategyRegistry;
import com.example.finalproject.global.config.CookieUtil;
import com.example.finalproject.global.jwt.JwtProperties;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;


@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SocialLoginStrategyRegistry strategyRegistry;
    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2User oauth2User)) {
            try {
                super.onAuthenticationSuccess(request, response, authentication);
            } catch (Exception e) {
                throw new RuntimeException("OAuth2 success handling failed", e);
            }
            return;
        }

        String registrationId = authentication instanceof OAuth2AuthenticationToken token
                ? token.getAuthorizedClientRegistrationId()
                : null;
        if (registrationId == null || registrationId.isBlank()) {
            registrationId = "kakao";
        }

        SocialLoginStrategy strategy = strategyRegistry.getRequiredStrategy(registrationId);
        String redirectUrl = defaultIfBlank(strategy.frontendSuccessUrl(), "http://localhost:5173");

        Map<String, Object> attributes = oauth2User.getAttributes();
        String providerUserId = strategy.resolveProviderUserId(attributes);
        if ((providerUserId == null || providerUserId.isBlank()) && "naver".equals(registrationId)) {
            providerUserId = oauth2User.getName();
        }
        String nickname = strategy.resolveNickname(attributes);

        if (providerUserId != null && !providerUserId.isBlank()) {
            if (strategy.isRegistered(providerUserId)) {
                log.info("[OAuth2 {}] 기존 회원 로그인 처리( JWT 발급 ) providerUserId={}", registrationId, providerUserId);
                OAuthLoginSessionResult result = strategy.findOrCreate(providerUserId, nickname);
                LoginResponse loginResponse = authService.issueTokensForUser(result.getUser(), result.getRoles());
                addTokenCookies(response, loginResponse);
                redirectUrl = appendStatusQuery(redirectUrl, registrationId, "success");
            } else {
                log.info("[OAuth2 {}] 최초 로그인 - 회원가입 폼 필요( state 토큰 리다이렉트 ) providerUserId={}", registrationId, providerUserId);
                String stateToken = jwtTokenProvider.generateSocialSignupStateToken(
                        registrationId, providerUserId, nickname != null ? nickname : "");
                redirectUrl = appendStatusQuery(redirectUrl, registrationId, "signup_required");
                redirectUrl = appendStateQuery(redirectUrl, stateToken);
            }
        } else {
            redirectUrl = appendStatusQuery(redirectUrl, registrationId, "success");
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void addTokenCookies(HttpServletResponse response, LoginResponse loginResponse) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtil.createAccessTokenCookie(
                        loginResponse.getAccessToken(),
                        jwtProperties.getAccessTokenValiditySeconds()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtil.createRefreshTokenCookie(
                        loginResponse.getRefreshToken(),
                        jwtProperties.getRefreshTokenValiditySeconds()).toString());
    }

    private String appendStateQuery(String redirectUrl, String stateToken) {
      String encoded = URLEncoder.encode(stateToken, StandardCharsets.UTF_8);
      return redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "state=" + encoded;
    }

    private String appendStatusQuery(String redirectUrl, String registrationId, String status) {
        String key = registrationId + "=";
        if (!redirectUrl.contains(key)) {
            return redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + registrationId + "=" + status;
        }
        return redirectUrl;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
