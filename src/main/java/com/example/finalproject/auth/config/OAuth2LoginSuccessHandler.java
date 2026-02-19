package com.example.finalproject.auth.config;

import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.service.KakaoService;
import com.example.finalproject.auth.social.SocialLoginSessionConstants;
import com.example.finalproject.auth.social.SocialLoginStrategy;
import com.example.finalproject.auth.social.SocialLoginStrategyRegistry;
import com.example.finalproject.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SocialLoginStrategyRegistry strategyRegistry;

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
        String nickname = strategy.resolveNickname(attributes);

        if (providerUserId != null && !providerUserId.isBlank()) {
            if (strategy.isRegistered(providerUserId)) {
                log.info("[OAuth2 {}] 기존 회원 로그인 처리 providerUserId={}", registrationId, providerUserId);
                OAuthLoginSessionResult result = strategy.findOrCreate(providerUserId, nickname);
                CustomUserDetails userDetails = new CustomUserDetails(result.getUser(), result.getRoles());
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                redirectUrl = appendStatusQuery(redirectUrl, registrationId, "success");
            } else {
                log.info("[OAuth2 {}] 최초 로그인 - 회원가입 폼 필요 providerUserId={}", registrationId, providerUserId);
                request.getSession().setAttribute(SocialLoginSessionConstants.PENDING_PROVIDER, registrationId);
                request.getSession().setAttribute(SocialLoginSessionConstants.PENDING_PROVIDER_USER_ID, providerUserId);
                request.getSession().setAttribute(SocialLoginSessionConstants.PENDING_NICKNAME, nickname != null ? nickname : "");
                redirectUrl = appendStatusQuery(redirectUrl, registrationId, "signup_required");
            }
        } else {
            redirectUrl = appendStatusQuery(redirectUrl, registrationId, "success");
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
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
