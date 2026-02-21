package com.example.finalproject.auth.controller;

import com.example.finalproject.auth.dto.DuplicateCheckResponse;
import com.example.finalproject.auth.dto.request.*;
import com.example.finalproject.auth.dto.response.MeResponse;
import com.example.finalproject.auth.dto.response.*;
import com.example.finalproject.auth.service.AuthService;
import com.example.finalproject.auth.dto.SocialSignupStateClaims;
import com.example.finalproject.auth.social.SocialLoginStrategy;
import com.example.finalproject.auth.social.SocialLoginStrategyRegistry;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import com.example.finalproject.global.config.CookieUtil;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.jwt.JwtProperties;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final SocialLoginStrategyRegistry socialLoginStrategyRegistry;
    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;

    /** 프로필 조회: 로그인 사용자 본인만 조회 가능. 미로그인 시 401. 이메일은 읽기 전용. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(Authentication authentication) {
        MeResponse me = authService.getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(me));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkEmail(
            @RequestParam @Email(message = "이메일 형식이 올바르지 않습니다.") String email
    ) {
        boolean duplicated = authService.isEmailDuplicated(email); 
        return ResponseEntity.ok(ApiResponse.success(new DuplicateCheckResponse(duplicated)));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkPhone(
            @RequestParam @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.") String phone
    ) {
        boolean duplicated = authService.isPhoneDuplicated(phone);
        return ResponseEntity.ok(ApiResponse.success(new DuplicateCheckResponse(duplicated)));
    }

     //AccessToken/RefreshToken은 HttpOnly Cookie로
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SignupResponse>> register(
            @Valid @RequestBody SignupRequest request) {

        SignupResponse response = authService.register(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createAccessTokenCookie(
                        response.getAccessToken(),
                        jwtProperties.getAccessTokenValiditySeconds()).toString());
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createRefreshTokenCookie(
                        response.getRefreshToken(),
                        jwtProperties.getRefreshTokenValiditySeconds()).toString());
        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createAccessTokenCookie(
                        response.getAccessToken(),
                        jwtProperties.getAccessTokenValiditySeconds()).toString());
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createRefreshTokenCookie(
                        response.getRefreshToken(),
                        jwtProperties.getRefreshTokenValiditySeconds()).toString());
        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("로그인이 완료되었습니다.", response));
    }

    @PostMapping("/send-verification")
    public ResponseEntity<ApiResponse<SendVerificationResponse>> sendVerification(
            @Valid @RequestBody SendVerificationRequest request
    ) {
        SendVerificationResponse response = authService.sendVerification(request.getPhone());
        return ResponseEntity.ok(ApiResponse.success(response.getMessage(), response));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<VerifyPhoneResponse>> verifyPhone(
            @Valid @RequestBody VerifyPhoneRequest request
    ) {
        String token = authService.verifyPhone(request.getPhone(), request.getVerificationCode());
        return ResponseEntity.ok(ApiResponse.success("휴대폰 인증이 완료되었습니다.", new VerifyPhoneResponse(true, token)));
    }

    // 소셜(카카오/네이버) 최초 로그인 후 추가 회원가입 폼 제출. state(JWT)로 provider 정보 전달.
    @PostMapping("/social-signup/complete")
    public ResponseEntity<ApiResponse<MeResponse>> completeSocialSignup(
            @Valid @RequestBody SocialSignupCompleteRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        String stateToken = request.getState();
        if (stateToken == null || stateToken.isBlank()) {
            log.warn("[소셜 가입 완료] state 토큰 없음");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        SocialSignupStateClaims stateClaims;
        try {
            stateClaims = jwtTokenProvider.parseSocialSignupStateToken(stateToken);
        } catch (Exception e) {
            log.warn("[소셜 가입 완료] state 토큰 검증 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        String registrationId = stateClaims.registrationId();
        String providerUserId = stateClaims.providerUserId();
        if (registrationId.isBlank() || providerUserId.isBlank()) {
            log.warn("[소셜 가입 완료] state 클레임 불완전");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        SocialLoginStrategy strategy = socialLoginStrategyRegistry.getRequiredStrategy(registrationId);
        log.info("[소셜 가입 완료] API 호출 provider={}, providerUserId={} (JWT state)", registrationId, providerUserId);
        OAuthLoginSessionResult result = strategy.completeSignup(providerUserId, request);

        LoginResponse loginResponse = authService.issueTokensForUser(result.getUser(), result.getRoles());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createAccessTokenCookie(
                        loginResponse.getAccessToken(),
                        jwtProperties.getAccessTokenValiditySeconds()).toString());
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createRefreshTokenCookie(
                        loginResponse.getRefreshToken(),
                        jwtProperties.getRefreshTokenValiditySeconds()).toString());

        MeResponse me = new MeResponse(
                result.getUser().getId(),
                result.getUser().getEmail(),
                result.getUser().getName(),
                result.getUser().getPhone(),
                result.getUser().getCreatedAt(),
                result.getRoles());
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", me));
    }

    //refreshToken -> Cookie에서 읽음 (HttpOnly)
    //갱신된 토큰은 HttpOnly Cookie로
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refresh(
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE, required = false) String refreshToken) {
        TokenRefreshResponse response = authService.refresh(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createAccessTokenCookie(
                        response.getAccessToken(),
                        jwtProperties.getAccessTokenValiditySeconds()).toString());
        headers.add(HttpHeaders.SET_COOKIE,
                CookieUtil.createRefreshTokenCookie(
                        response.getRefreshToken(),
                        jwtProperties.getRefreshTokenValiditySeconds()).toString());
        return ResponseEntity.ok().headers(headers).body(ApiResponse.success("토큰이 갱신되었습니다.", null));
    }


    // RT or Cookie(nm_refreshToken) 중 하나로 무효화. 토큰이 없어도 200 + 쿠키 삭제 (프론트 로그아웃 UI 항상 성공)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE, required = false) String refreshTokenFromCookie) {
        String token = (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank())
                ? request.getRefreshToken() : refreshTokenFromCookie;
        if (token != null && !token.isBlank()) {
            authService.logout(token);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.clearAccessTokenCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.clearRefreshTokenCookie().toString());
        headers.add("Clear-Site-Data", "\"cookies\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponse.success("로그아웃 되었습니다."));
    }

    /** 브라우저 쿠키 삭제용. 로그아웃 후 프론트에서 한 번 더 호출해 쿠키 삭제를 확실히 함. */
    @GetMapping("/clear-cookies")
    public ResponseEntity<Void> clearCookies() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.clearAccessTokenCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, CookieUtil.clearRefreshTokenCookie().toString());
        headers.add("Clear-Site-Data", "\"cookies\"");
        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

}
