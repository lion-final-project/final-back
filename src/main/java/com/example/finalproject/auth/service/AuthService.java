package com.example.finalproject.auth.service;

import com.example.finalproject.auth.dto.request.SignupRequest;
import com.example.finalproject.auth.dto.response.SendVerificationResponse;
import com.example.finalproject.auth.dto.response.SignupResponse;
import com.example.finalproject.auth.dto.response.TokenRefreshResponse;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import com.example.finalproject.user.domain.Role;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserRole;
import com.example.finalproject.user.repository.RoleRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int VERIFICATION_EXPIRE_MINUTES = 3;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final SmsService smsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isPhoneDuplicated(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Transactional
    public SignupResponse register(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        smsService.validateAndConsumeVerificationToken(
                request.getPhone(), request.getPhoneVerificationToken());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .termsAgreed(request.isTermsAgreed())
                .privacyAgreed(request.isPrivacyAgreed())
                .termsAgreedAt(request.isTermsAgreed() ? now : null)
                .privacyAgreedAt(request.isPrivacyAgreed() ? now : null)
                .build();
        User savedUser = userRepository.save(user);

        Role role = roleRepository.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("CUSTOMER").build()));
        UserRole userRole = UserRole.builder()
                .user(savedUser)
                .role(role)
                .build();
        userRoleRepository.save(userRole);

        List<String> roles = List.of(role.getRoleName());
        String accessToken = jwtTokenProvider.generateAccessToken(savedUser, roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser, roles);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                roles,
                accessToken,
                refreshToken
        ); 
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank() || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID); //refreshToken 유효성 검사
        }
        String email = jwtTokenProvider.parseClaims(refreshToken).getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID)); //user 조회
        List<String> roles = user.getUserRoles().stream() 
                .map(ur -> ur.getRole().getRoleName()) //user의 역할 조회
                .toList();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user, roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, roles);
        return new TokenRefreshResponse(newAccessToken, newRefreshToken); //새로운 토큰 발급
    }

    //휴대폰 인증번호 발송 (CoolSMS + Redis)
    public SendVerificationResponse sendVerification(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        smsService.sendAuthCode(phone);
        int remaining = smsService.getRemainingResendAttempts(phone);
        int expiresInSeconds = VERIFICATION_EXPIRE_MINUTES * 60; //3분

        return new SendVerificationResponse(
                "인증번호가 발송되었습니다.",
                expiresInSeconds,
                remaining
        );
    }

    //휴대폰 인증번호 검증 (Redis) → 인증 완료 토큰 발급 (회원가입 시 사용)
    public String verifyPhone(String phone, String code) {
        return smsService.verifyAuthCode(phone, code);
    }
}
