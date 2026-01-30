package com.example.finalproject.auth.controller;

import com.example.finalproject.auth.dto.DuplicateCheckResponse;
import com.example.finalproject.auth.dto.request.SignupRequest;
import com.example.finalproject.auth.service.AuthService;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

    /**
     * 회원가입: 입력값 검증 후 DB 저장
     * - @Valid 로 형식/필수값/약관동의 검증
     * - 비밀번호 BCrypt 암호화 후 User 저장
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody SignupRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkEmailDuplicate(
            @RequestParam
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {
        boolean duplicated = authService.isEmailDuplicated(email);
        return ResponseEntity.ok(ApiResponse.success(new DuplicateCheckResponse(duplicated)));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<ApiResponse<DuplicateCheckResponse>> checkPhoneDuplicate(
            @RequestParam
            @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
            String phone
    ) {
        boolean duplicated = authService.isPhoneDuplicated(phone);
        return ResponseEntity.ok(ApiResponse.success(new DuplicateCheckResponse(duplicated)));
    }
}
