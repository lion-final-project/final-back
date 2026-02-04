package com.example.finalproject.auth.service;

import com.example.finalproject.auth.config.KakaoProperties;
import com.example.finalproject.auth.domain.RefreshToken;
import com.example.finalproject.auth.dto.kakao.KakaoTokenResponseDto;
import com.example.finalproject.auth.dto.kakao.KakaoUserInfoResponse;
import com.example.finalproject.auth.dto.kakao.OAuthLoginSessionResult;
import com.example.finalproject.auth.dto.request.SocialSignupCompleteRequest;
import com.example.finalproject.auth.dto.response.LoginResponse;
import com.example.finalproject.auth.repository.RefreshTokenRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.jwt.JwtTokenProvider;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.Role;
import com.example.finalproject.user.domain.SocialLogin;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.domain.UserRole;
import com.example.finalproject.user.enums.SocialProvider;
import com.example.finalproject.user.repository.AddressRepository;
import com.example.finalproject.user.repository.RoleRepository;
import com.example.finalproject.user.repository.SocialLoginRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.repository.UserRoleRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private static final String KAKAO_AUTH_URI = "https://kauth.kakao.com";
    private static final String KAKAO_API_URI = "https://kapi.kakao.com";

    private final KakaoProperties kakaoProperties;
    private final UserRepository userRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AddressRepository addressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public LoginResponse loginWithCode(String code) {
        String accessToken = requestAccessToken(code);
        KakaoUserInfoResponse userInfo = requestUserInfo(accessToken);

        Long kakaoId = userInfo.getId();
        String providerUserId = String.valueOf(kakaoId);
        String nickname = userInfo.getNickname();

        User user = socialLoginRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, providerUserId)
                .map(SocialLogin::getUser)
                .orElseGet(() -> registerKakaoUser(providerUserId, nickname));

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

        String newAccessToken = jwtTokenProvider.generateAccessToken(user, roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user, roles);
        refreshTokenRepository.deleteByUser(user);
        saveRefreshToken(user, newRefreshToken);

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles,
                newAccessToken,
                newRefreshToken
        );
    }


    @Transactional
    public OAuthLoginSessionResult getUserAndRolesForSession(String code) {
        String accessToken = requestAccessToken(code);
        KakaoUserInfoResponse userInfo = requestUserInfo(accessToken);

        String providerUserId = String.valueOf(userInfo.getId());
        String nickname = userInfo.getNickname();

        User user = socialLoginRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, providerUserId)
                .map(SocialLogin::getUser)
                .orElseGet(() -> registerKakaoUser(providerUserId, nickname));

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

    // 카카오 최초 로그인 여부. true = 이미 가입됨, false = 회원가입 폼 필요
    public boolean isKakaoUserRegistered(String providerUserId) {
        return socialLoginRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, providerUserId).isPresent();
    }

    //Spring OAuth2 Client용: 카카오에서 받은 id·닉네임으로 우리 User 조회. 이미 가입된 경우만 사용
    @Transactional
    public OAuthLoginSessionResult findOrCreateByKakaoInfo(String providerUserId, String nickname) {
        User user = socialLoginRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, providerUserId)
                .map(SocialLogin::getUser)
                .orElseGet(() -> registerKakaoUser(providerUserId, nickname));

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

    //카카오 최초 로그인 후 회원가입 폼 제출 처리. User + Address + SocialLogin 동일하게 반영
    @Transactional
    public OAuthLoginSessionResult completeKakaoSignup(String providerUserId, SocialSignupCompleteRequest request) {
        if (socialLoginRepository.findByProviderAndProviderUserId(SocialProvider.KAKAO, providerUserId).isPresent()) {
            throw new IllegalStateException("이미 카카오로 가입된 회원입니다.");
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
                .provider(SocialProvider.KAKAO)
                .providerUserId(providerUserId)
                .connectedAt(now)
                .build());

        String line2 = request.getAddressLine2() != null ? request.getAddressLine2() : "";
        addressRepository.save(Address.builder()
                .user(user)
                .contact(request.getPhone())
                .addressName("기본")
                .postalCode("00000")
                .addressLine1(request.getAddressLine1())
                .addressLine2(line2)
                .location(null)
                .isDefault(true)
                .build());

        List<String> roles = List.of(role.getRoleName());
        return new OAuthLoginSessionResult(user, roles);
    }

    private String requestAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProperties.getClientId());
        params.add("redirect_uri", kakaoProperties.getRedirectUri());
        params.add("code", code);
        if (kakaoProperties.getClientSecret() != null && !kakaoProperties.getClientSecret().isBlank()) {
            params.add("client_secret", kakaoProperties.getClientSecret());
        }

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<KakaoTokenResponseDto> response = restTemplate.postForEntity(
                KAKAO_AUTH_URI + "/oauth/token",
                entity,
                KakaoTokenResponseDto.class
        );

        KakaoTokenResponseDto body = response.getBody();
        if (body == null || body.getAccessToken() == null) {
            throw new IllegalStateException("카카오 토큰 응답 오류");
        }
        return body.getAccessToken();
    }

    private KakaoUserInfoResponse requestUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                KAKAO_API_URI + "/v2/user/me",
                HttpMethod.GET,
                entity,
                KakaoUserInfoResponse.class
        );

        KakaoUserInfoResponse body = response.getBody();
        if (body == null || body.getId() == null) {
            throw new IllegalStateException("카카오 사용자 정보 응답 오류");
        }
        return body;
    }

    private User registerKakaoUser(String providerUserId, String nickname) {
        String email = "kakao_" + providerUserId + "@kakao.local";
        String name = (nickname != null && !nickname.isBlank()) ? nickname : "카카오사용자";
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
                .provider(SocialProvider.KAKAO)
                .providerUserId(providerUserId)
                .connectedAt(LocalDateTime.now())
                .build());

        return user;
    }

    private String generatePlaceholderPhone(String providerUserId) {
        String base = "kakao-" + providerUserId;
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }
        if (!userRepository.existsByPhone(base)) {
            return base;
        }
        return "kakao-" + UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }

    private void saveRefreshToken(User user, String refreshToken) {
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                jwtTokenProvider.parseClaims(refreshToken).getExpiration().toInstant(),
                ZoneId.systemDefault()
        );
        refreshTokenRepository.save(new RefreshToken(user, refreshToken, expiresAt));
    }
}
