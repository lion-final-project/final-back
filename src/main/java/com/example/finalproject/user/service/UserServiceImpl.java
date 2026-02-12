package com.example.finalproject.user.service;

import com.example.finalproject.auth.repository.RefreshTokenRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.security.CustomUserDetails;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import com.example.finalproject.user.dto.response.GetWithdrawalCheckResponse;
import com.example.finalproject.user.dto.response.PostWithdrawalConfirmResponse;
import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.repository.AddressRepository;
import com.example.finalproject.user.repository.SocialLoginRepository;
import com.example.finalproject.user.repository.UserRepository;
import com.example.finalproject.user.service.interfaces.UserService;
import com.example.finalproject.user.withdrawal.dto.BlockedReason;
import com.example.finalproject.user.withdrawal.rule.WithdrawalEligibilityRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String ALREADY_INACTIVE_CODE = "ALREADY_INACTIVE";
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AddressRepository addressRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final List<WithdrawalEligibilityRule> withdrawalEligibilityRules;

    @Override
    public Slice<StoreNearbyResponse> getNearbyStores(GetStoreSearchRequest request) {
        return storeRepository.findNearbyStoresByCategory(request);
    }

    //회원탈퇴 사전 가능 여부 조회
    @Override
    @Transactional(readOnly = true)
    public GetWithdrawalCheckResponse checkWithdrawalEligibility(Authentication authentication) {
        User user = getCurrentUser(authentication);
        log.info("[회원탈퇴] 탈퇴 가능 여부 조회 시작 userId={}", user.getId());


        List<BlockedReason> blockedReasons = evaluateBlockedReasons(user);

        if (blockedReasons.isEmpty()) {
            log.info("[회원탈퇴] 탈퇴 가능 userId={}", user.getId());
        } else {
            log.warn("[회원탈퇴] 회원탈퇴 불가 userId={}, 사유={}", user.getId(),blockedReasons.stream().map(BlockedReason::getCode).toList());
        }

        return GetWithdrawalCheckResponse.builder()
                .canWithdraw(blockedReasons.isEmpty())
                .blockedReasons(blockedReasons)
                .build();
    }

    // 회원탈퇴 확정 처리(재검증 + 소프트삭제 + 토큰/세션 무효화)
    @Override
    @Transactional
    public PostWithdrawalConfirmResponse withdraw(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<BlockedReason> blockedReasons = evaluateBlockedReasons(user);

        if (!blockedReasons.isEmpty()) {
            log.warn("[회원탈퇴] 회원탈퇴 불가 - 확정 요청 거부 userId={}, 사유={}", user.getId(), blockedReasons.stream().map(BlockedReason::getCode).toList());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        LocalDateTime now = LocalDateTime.now();
        user.deactive(now);
        user.maskPersonalInfoForWithdrawal(now);

        addressRepository.deleteAllByUserId(user.getId());
        socialLoginRepository.deleteAllByUserId(user.getId());
        refreshTokenRepository.deleteByUser(user);

        log.info("[회원탈퇴] 정상 회원탈퇴 완료 userId={}, deletedAt={}", user.getId(), user.getDeletedAt());

        return PostWithdrawalConfirmResponse.builder()
                .message("회원 탈퇴가 완료되었습니다.")
                .loggedOut(true)
                .nextAction("REDIRECT_LOGIN")
                .build();
    }

private List<BlockedReason> evaluateBlockedReasons(User user) {
    List<BlockedReason> blockedReasons = new ArrayList<>();

    if (user.getStatus() != UserStatus.ACTIVE || user.getDeletedAt() != null) {
        blockedReasons.add(BlockedReason.builder()
                .code(ALREADY_INACTIVE_CODE)
                .message("이미 탈퇴했거나 비활성화된 계정입니다.")
                .build());
    }

    for (WithdrawalEligibilityRule rule : withdrawalEligibilityRules) {
        rule.validate(user).ifPresent(blockedReasons::add);
    }

    return blockedReasons;
}

    //현재 사용자 조회
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return userRepository.findById(details.getUser().getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }
        if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

}
