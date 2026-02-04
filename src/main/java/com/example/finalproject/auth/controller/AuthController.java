package com.example.finalproject.auth.controller;

import com.example.finalproject.auth.dto.DuplicateCheckResponse;
import com.example.finalproject.auth.dto.request.SendVerificationRequest;
import com.example.finalproject.auth.dto.request.SignupRequest;
import com.example.finalproject.auth.dto.request.VerifyPhoneRequest;
import com.example.finalproject.auth.dto.response.SendVerificationResponse;
import com.example.finalproject.auth.dto.response.SignupResponse;
import com.example.finalproject.auth.dto.response.TokenRefreshResponse;
import com.example.finalproject.auth.dto.response.VerifyPhoneResponse;
import com.example.finalproject.auth.service.AuthService;
import com.example.finalproject.global.config.CookieUtil;
import com.example.finalproject.global.jwt.JwtProperties;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

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
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", response));
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
}
