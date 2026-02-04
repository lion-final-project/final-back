package com.example.finalproject.auth.service;

import com.example.finalproject.auth.domain.RefreshToken;
import com.example.finalproject.auth.dto.request.LoginRequest;
import com.example.finalproject.auth.dto.request.SignupRequest;
import com.example.finalproject.auth.dto.response.LoginResponse;
import com.example.finalproject.auth.dto.response.MeResponse;
import com.example.finalproject.auth.dto.response.SendVerificationResponse;
import com.example.finalproject.auth.dto.response.SignupResponse;
import com.example.finalproject.auth.dto.response.TokenRefreshResponse;
import com.example.finalproject.auth.repository.RefreshTokenRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import com.example.finalproject.user.domain.Role;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserRole;
import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.repository.RoleRepository;
import com.example.finalproject.global.security.CustomUserDetails;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    private final RefreshTokenRepository refreshTokenRepository;
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

        // 방법 1: phoneVerificationToken 있으면 토큰 검증, 없으면 인증 완료 후 5분 TTL(SMS:PHONE_VERIFIED) 검증
        String token = request.getPhoneVerificationToken();
        if (token != null && !token.isBlank()) {
            smsService.validateAndConsumeVerificationToken(request.getPhone(), token);
        } else {
            smsService.validateAndConsumePhoneVerified(request.getPhone());
        }

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

        saveRefreshToken(savedUser, refreshToken);

        return new SignupResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                roles,
                accessToken,
                refreshToken
        ); 
    }

    //로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        if (user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_STATUS_FORBIDDEN);
        }
        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getRoleName)
                .toList();
        String accessToken = jwtTokenProvider.generateAccessToken(user, roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, roles);
        refreshTokenRepository.deleteByUser(user);
        saveRefreshToken(user, refreshToken);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles,
                accessToken,
                refreshToken
        );
    }

    //토큰 갱신
    @Transactional
    public TokenRefreshResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank() || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID); //refreshToken 유효성 검사
        }
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        User user = storedToken.getUser();
        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getRoleName)
                .toList();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user, roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, roles);
        storedToken.updateToken(newRefreshToken, extractExpiry(newRefreshToken));
        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    //로그아웃
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_MISSING);
        }
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken) 
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        refreshTokenRepository.delete(storedToken); 
    }

    /** 세션(JWT) 현재 사용자 정보. CustomUserDetails(세션) 또는 JWT subject(이메일) 지원 */
    @Transactional(readOnly = true)
    public MeResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            User user = details.getUser();
            List<String> roles = details.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .toList();
            return new MeResponse(user.getId(), user.getEmail(), user.getName(), roles);
        }
        if (principal instanceof String email) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
            List<String> roles = user.getUserRoles().stream()
                    .map(UserRole::getRole)
                    .map(Role::getRoleName)
                    .toList();
            return new MeResponse(user.getId(), user.getEmail(), user.getName(), roles);
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
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

    private void saveRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(user, refreshToken, extractExpiry(refreshToken)));
    }

    //토큰 만료 시간
    private LocalDateTime extractExpiry(String token) {
        return LocalDateTime.ofInstant(
                jwtTokenProvider.parseClaims(token).getExpiration().toInstant(),
                ZoneId.systemDefault()
        );
    }

}
