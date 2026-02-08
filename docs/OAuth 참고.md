# OAuth

```java
package com.example.backend.auth.oauth.attributes;

import java.util.Map;

public class KakaoOAuthAttributes implements OAuthAttributes {
    private final Map<String, Object> attributes;

    public KakaoOAuthAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider(){
        return "kakao";
    }

    @Override
    public String getProviderUserId(){
        return String.valueOf(attributes.get("id"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> account =
                (Map<String, Object>) attributes.get("kakao_account");

        if (account == null) {
            return null;
        }

        return (String) account.get("email");
    }
}
```

```java
package com.example.backend.auth.oauth.attributes;

import java.util.Map;

public class OAuthAttributesFactory {
    public static OAuthAttributes of(
            String provider,
            Map<String, Object> attributes
    ){
        return switch (provider){
            case "google"->new GoogleOAuthAttributes(attributes);
            case "kakao"->new KakaoOAuthAttributes(attributes);
            case "naver"->new NaverOAuthAttributes(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth provider:  " + provider);
        };
    }
}
```

```java
package com.example.backend.auth.oauth;

import com.example.backend.auth.facade.AuthMemberFacade;
import com.example.backend.auth.oauth.attributes.OAuthAttributes;
import com.example.backend.auth.oauth.attributes.OAuthAttributesFactory;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AuthMemberFacade  authMemberFacade;
    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        //OAuth 제공자로부터 사용자 정보 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);
        //OAuth provider 식별
        String provider=userRequest.getClientRegistration().getRegistrationId();

        //provider별 attribute 정규화
        OAuthAttributes attributes= OAuthAttributesFactory.of(provider,oAuth2User.getAttributes());

        String providerUserId= attributes.getProviderUserId();
        String email= attributes.getEmail();

        boolean isMember;
        try {
            memberService.findRegisteredMemberByOAuth(provider, providerUserId);
            isMember = true;
        } catch (BusinessException e) {
            isMember = false;
        }
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(provider, providerUserId, email, isMember);

        return customOAuth2User;
    }

    private OAuth2AuthenticationException toOAuth2AuthException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        OAuth2Error oauth2Error = new OAuth2Error(
                errorCode.name(),
                errorCode.getMessage(),
                null
        );
        return new OAuth2AuthenticationException(oauth2Error, errorCode.getMessage());
    }
}
```

```java
package com.example.backend.auth.oauth;

import com.example.backend.auth.oauth.service.OAuthTempTokenService;
import com.example.backend.global.security.CustomUserDetails;
import com.example.backend.member.entity.Member;
import com.example.backend.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-base-url:http://52.79.142.181:3000}")
    private String frontendBaseUrl;
    private final MemberService memberService;
    private final OAuthTempTokenService oAuthTempTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) oauthToken.getPrincipal();

        if (customOAuth2User.isMember()) {
            Member member = memberService.findRegisteredMemberByOAuth(customOAuth2User.getProvider(), customOAuth2User.getProviderUserId());

            // 세션 설정 (CustomUserDetails 사용)
            CustomUserDetails userDetails = new CustomUserDetails(
                    member.getId(),
                    member.getEmail(),
                    member.getPassword(),
                    member.getRoles()
            );

            Authentication oauthAuthentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // SecurityContext에 저장
            SecurityContextHolder.getContext().setAuthentication(oauthAuthentication);

            // 세션에 SecurityContext 저장 (일반 로그인과 동일한 방식)
            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
            log.info("successHandler session ID = {} ", session.getId());
            response.sendRedirect(frontendBaseUrl);
        } else {
            // 비회원인 경우 임시 토큰 생성하여 프론트엔드로 전달
            String token = oAuthTempTokenService.createToken(
                    customOAuth2User.getProvider(),
                    customOAuth2User.getProviderUserId(),
                    customOAuth2User.getEmail()
            );

            // 토큰과 이메일을 쿼리 파라미터로 전달
            String redirectUrl = frontendBaseUrl + "/members/oauth/complete-profile?token="
                    + URLEncoder.encode(token, StandardCharsets.UTF_8);
            
            // 이메일이 있는 경우 쿼리 파라미터에 추가
            if (customOAuth2User.getEmail() != null && !customOAuth2User.getEmail().isEmpty()) {
                redirectUrl += "&email=" + URLEncoder.encode(customOAuth2User.getEmail(), StandardCharsets.UTF_8);
            }
            
            response.sendRedirect(redirectUrl);
        }

    }
}
```

```java
package com.example.backend.auth.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth 로그인 실패 시 처리
 * - 기본 로그인 페이지(/login)로 리다이렉트
 * - 에러 코드를 쿼리로 전달해서 프론트/페이지에서 메시지 매핑 가능
 */
@Component
public class OAuthLoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            String code = oauth2Ex.getError().getErrorCode();
            response.sendRedirect(frontendBaseUrl + "/login?error&code=" + urlEncode(code));
            return;
        }

        response.sendRedirect(frontendBaseUrl + "/login?error");
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}

```

```java
package com.example.backend.auth.oauth.service;

import com.example.backend.auth.oauth.entity.OAuthTempToken;
import com.example.backend.auth.oauth.repository.OAuthTempTokenRepository;
import com.example.backend.global.exception.BusinessException;
import com.example.backend.global.exception.ErrorCode;
import com.example.backend.member.dto.request.CompleteOAuthProfileRequest;
import com.example.backend.member.entity.Member;
import com.example.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthTempTokenService {

    private final OAuthTempTokenRepository tokenRepository;
    private final MemberService memberService;

    // 토큰 유효 시간 (10분)
    private static final int TOKEN_EXPIRY_MINUTES = 10;

    /**
     * OAuth 임시 토큰 생성
     *
     * @param provider       OAuth 제공자 (google, kakao 등)
     * @param providerUserId OAuth 제공자의 사용자 ID
     * @param email          사용자 이메일
     * @return 생성된 토큰 문자열
     */
    public String createToken(String provider, String providerUserId, String email) {
        try {
            // 새 토큰 생성 (UUID)
            String token = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);

            OAuthTempToken tempToken = OAuthTempToken.builder()
                    .token(token)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .email(email)
                    .expiryDate(expiryDate)
                    .build();

            tokenRepository.save(tempToken);

            log.info("OAuth 임시 토큰 생성: provider={}, email={}", provider, email);
            return token;
        } catch (DataIntegrityViolationException e) {
            log.warn("OAuth 임시 토큰 생성 실패 (중복): provider={}, email={}", provider, email);
            // 중복 발생 시 재시도 (매우 드문 경우)
            return createToken(provider, providerUserId, email);
        }
    }

    /**
     * 토큰 검증 및 정보 반환
     *
     * @param token 검증할 토큰
     * @return OAuthTempToken (검증 성공 시)
     * @throws BusinessException 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional(readOnly = true)
    public OAuthTempToken validateToken(String token) {
        OAuthTempToken tempToken = tokenRepository.findByTokenWithLock(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!tempToken.isValid()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        return tempToken;
    }

    /**
     * 토큰 사용 처리 (일회용)
     *
     * @param token 사용할 토큰
     */
    public void markTokenAsUsed(String token) {
        OAuthTempToken tempToken = validateToken(token);
        tempToken.markAsUsed();
        log.info("OAuth 임시 토큰 사용 처리: token={}", token);
    }

    /**
     * 만료된 토큰 정리 (매일 자정 실행)
     */
    @Scheduled(cron = "0 0 0 * * *")  // 매일 00:00:00에 실행
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByExpiryDateBefore(now);
        log.info("만료된 OAuth 임시 토큰 정리 완료");
    }

    public Member completeOAuthRegistration(String token, CompleteOAuthProfileRequest request) {
        log.info("OAuth 프로필 완성 요청");

        OAuthTempToken tempToken = validateToken(token);
        String eamil;
        if (tempToken.getEmail() == null) {
            eamil= request.getEmail();
        } else {
            eamil = tempToken.getEmail();
        }

        // 회원 가입 및 프로필 완성
        Member member = memberService.registerOAuthMember(
                tempToken.getProvider(),
                tempToken.getProviderUserId(),
                eamil,
                request.getNickname(),
                request.getBirthYear(),
                request.getGender()
        );
        log.info("member id = {}, tempToken provider = {}", member.getId(), tempToken.getProvider());

        // 토큰 사용 처리
        markTokenAsUsed(token);
        return member;
    }
}
```

```java
package com.example.backend.auth.oauth.controller;

import com.example.backend.auth.oauth.service.OAuthTempTokenService;
import com.example.backend.global.security.CustomUserDetails;
import com.example.backend.member.dto.request.CompleteOAuthProfileRequest;
import com.example.backend.member.dto.response.MyInfoResponse;
import com.example.backend.member.entity.Member;
import com.example.backend.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    private final OAuthTempTokenService oAuthTempTokenService;
    private final MemberService memberService;

    /**
     * OAuth 프로필 완성 (추가 정보 입력)
     */
    @PostMapping("/complete-profile")
    public ResponseEntity<MyInfoResponse> completeOAuthProfile(
            @RequestParam String token,
            @Valid @RequestBody CompleteOAuthProfileRequest request,
            HttpServletRequest httpRequest) {
        Member completedMember = oAuthTempTokenService.completeOAuthRegistration(token,request);

        // 세션 설정 (CustomUserDetails 사용)
        CustomUserDetails userDetails = new CustomUserDetails(
                completedMember.getId(),
                completedMember.getEmail(),
                completedMember.getPassword(),
                completedMember.getRoles()
        );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        log.info("OAuth 회원가입 완료: email={}, sessionId={}", completedMember.getEmail(), session.getId());

        return ResponseEntity.ok(MyInfoResponse.fromEntity(completedMember));
    }
}
```

```java
application-local.yml

  #oauth
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${SPRING_GOOGLE_CLIENT_ID}
            client-secret: ${SPRING_GOOGLE_CLIENT_SECRET}
            scope:
              - email

          naver:
            client-id: ${SPRING_NAVER_CLIENT_ID}
            client-secret: ${SPRING_NAVER_CLIENT_SECRET}
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/naver"
            scope:
              - email

          kakao:
            client-id: ${SPRING_KAKAO_CLIENT_ID}
            client-secret: ${SPRING_KAKAO_CLIENT_SECRET}
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/kakao"
            scope:

        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response

          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

```
SPRING_KAKAO_CLIENT_ID: ${KAKAO_CLIENT_ID}
      SPRING_KAKAO_CLIENT_SECRET: ${KAKAO_CLIENT_SECRET}
```

```
KAKAO_CLIENT_ID=${{ secrets.KAKAO_CLIENT_ID }}
            KAKAO_CLIENT_SECRET=${{ secrets.KAKAO_CLIENT_SECRET }}
```

```java
 OAuth 임시 가입 - 이메일 제공됨 (카카오)
POST {{testUrl}}/oauth/register?provider=KAKAO&providerUserId=kakao123456&email=kakao@example.com

POST {{testUrl}}/2/complete-profile
Content-Type: {{contentType}}

{
  "nickname": "카카오유저",
  "gender": "FEMALE",
  "birthYear": 1995
}

```