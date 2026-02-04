package com.example.finalproject.auth.config;

import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.service.KakaoService;
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

    private final KakaoService kakaoService;
    private final KakaoProperties kakaoProperties;

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
        if (registrationId == null) {
            registrationId = "kakao";
        }

        String redirectUrl = kakaoProperties.getFrontendSuccessUrl();
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = "http://localhost:5173";
        }

        if ("kakao".equals(registrationId)) {
            Map<String, Object> attrs = oauth2User.getAttributes();
            Object idObj = attrs.get("id");
            String providerUserId = idObj != null ? String.valueOf(idObj) : null;
            String nickname = resolveNickname(attrs);

            if (providerUserId != null && !providerUserId.isBlank()) {
                // 최초 로그인도 자동 가입(플레이스홀더 정보) 후 로그인 처리. 회원가입 폼 없이 소셜 로그인만 사용.
                OAuthLoginSessionResult result = kakaoService.findOrCreateByKakaoInfo(providerUserId, nickname);
                CustomUserDetails userDetails = new CustomUserDetails(result.getUser(), result.getRoles());
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        }

        redirectUrl = redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "kakao=success";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String resolveNickname(Map<String, Object> attrs) {
        Object props = attrs.get("properties");
        if (props instanceof Map<?, ?> map) {
            Object n = map.get("nickname");
            if (n != null) return n.toString();
        }
        Object kakaoAccount = attrs.get("kakao_account");
        if (kakaoAccount instanceof Map<?, ?> account) {
            Object profile = account.get("profile");
            if (profile instanceof Map<?, ?> p) {
                Object n = p.get("nickname");
                if (n != null) return n.toString();
            }
        }
        return null;
    }
}
