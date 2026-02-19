package com.example.finalproject.user.controller;


import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.user.dto.request.PasswordResetRequest;
import com.example.finalproject.user.dto.request.UserPasswordResetConfirmRequest;
import com.example.finalproject.user.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService service;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> request(
            @Valid @RequestBody PasswordResetRequest dto) {
        service.requestReset(dto.getEmail());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 안내 메일을 발송했습니다."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(
            @Valid @RequestBody UserPasswordResetConfirmRequest dto) {
        service.confirmReset(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 성공적으로 변경되었습니다."));
    }
}

