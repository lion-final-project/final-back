package com.example.finalproject.auth.service;

import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.dto.request.SocialSignupCompleteRequest;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.user.domain.Role;
import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.domain.SocialLogin;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserRole;
import com.example.finalproject.user.enums.SocialProvider;
import com.example.finalproject.user.repository.RoleRepository;
import com.example.finalproject.user.repository.SocialLoginRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverService {

    private final UserRepository userRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isNaverUserRegistered(String providerUserId) {
        return socialLoginRepository
                .findByProviderAndProviderUserIdAndDeletedAtIsNull(SocialProvider.NAVER, providerUserId)
                .isPresent();
    }

    @Transactional
    public OAuthLoginSessionResult findOrCreateByNaverInfo(String providerUserId, String nickname) {
        User user = socialLoginRepository
                .findByProviderAndProviderUserIdAndDeletedAtIsNull(SocialProvider.NAVER, providerUserId)
                .map(SocialLogin::getUser)
                .orElseGet(() -> registerNaverUser(providerUserId, nickname));

        validateActiveUser(user);

        List<String> roles = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getRoleName)
                .toList();
        if (roles.isEmpty()) {
            Role role = roleRepository.findByRoleName("CUSTOMER")
                    .orElseGet(() -> roleRepository.save(Role.builder().roleName("CUSTOMER").build()));
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());
            roles = List.of(role.getRoleName());
        }
        return new OAuthLoginSessionResult(user, roles);
    }

    @Transactional
    public OAuthLoginSessionResult completeNaverSignup(String providerUserId, SocialSignupCompleteRequest request) {
        log.info("[네이버 소셜 가입] 시작 providerUserId={}, email={}", providerUserId, request.getEmail());
        if (socialLoginRepository
                .findByProviderAndProviderUserIdAndDeletedAtIsNull(SocialProvider.NAVER, providerUserId)
                .isPresent()) {
            log.warn("[네이버 소셜 가입] 이미 가입된 providerUserId={}", providerUserId);
            throw new IllegalStateException("이미 네이버로 가입된 회원입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        LocalDateTime now = LocalDateTime.now();
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        User user = userRepository.save(User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .phone(request.getPhone())
                .termsAgreed(request.isTermsAgreed())
                .privacyAgreed(request.isPrivacyAgreed())
                .termsAgreedAt(request.isTermsAgreed() ? now : null)
                .privacyAgreedAt(request.isPrivacyAgreed() ? now : null)
                .build());

        Role role = roleRepository.findByRoleName("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("CUSTOMER").build()));
        userRoleRepository.save(UserRole.builder().user(user).role(role).build());

        socialLoginRepository.save(SocialLogin.builder()
                .user(user)
                .provider(SocialProvider.NAVER)
                .providerUserId(providerUserId)
                .connectedAt(now)
                .build());

        List<String> roles = List.of(role.getRoleName());
        log.info("[네이버 소셜 가입] 완료 userId={}, providerUserId={}", user.getId(), providerUserId);
        return new OAuthLoginSessionResult(user, roles);
    }

    private User registerNaverUser(String providerUserId, String nickname) {
        String email = "naver_" + providerUserId + "@naver.local";
        String name = (nickname != null && !nickname.isBlank()) ? nickname : "네이버사용자";
        String phone = generatePlaceholderPhone(providerUserId);
        String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = userRepository.save(User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .phone(phone)
                .termsAgreed(false)
                .privacyAgreed(false)
                .build());

        socialLoginRepository.save(SocialLogin.builder()
                .user(user)
                .provider(SocialProvider.NAVER)
                .providerUserId(providerUserId)
                .connectedAt(LocalDateTime.now())
                .build());

        return user;
    }

    private String generatePlaceholderPhone(String providerUserId) {
        String base = "naver-" + providerUserId;
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }
        if (!userRepository.existsByPhone(base)) {
            return base;
        }
        return "naver-" + UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }

    /** 탈퇴/비활성 유저는 로그인 불가 (일반·카카오와 동일) */
    private void validateActiveUser(User user) {
        if (user.getStatus() != UserStatus.ACTIVE || user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.USER_STATUS_FORBIDDEN);
        }
    }
}
