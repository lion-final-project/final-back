package com.example.finalproject.subscription.controller;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.global.security.CustomUserDetails;
import com.example.finalproject.subscription.dto.response.GetSubscriptionResponse;
import com.example.finalproject.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * API-SUB-002: 고객 구독 목록 조회.
     * 로그인한 고객의 구독 목록을 조회한다. 구독 중/일시정지/해지 예정만 포함, 해지 완료는 제외.
     *
     * @return 200 OK, 구독 목록 (GetSubscriptionResponse)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetSubscriptionResponse>>> list() {
        String username = getCurrentUsername();
        List<GetSubscriptionResponse> list = subscriptionService.findListByUser(username);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * API-SUB-003: 구독 일시정지.
     * 본인 구독이며 ACTIVE 상태일 때만 가능.
     *
     * @param id 구독 ID (path)
     * @return 200 OK
     */
    @PatchMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<Void>> pause(@PathVariable Long id) {
        String username = getCurrentUsername();
        subscriptionService.pause(id, username);
        return ResponseEntity.ok(ApiResponse.success("구독이 일시정지되었습니다."));
    }

    /**
     * API-SUB-004: 구독 재개.
     * 본인 구독이며 PAUSED 상태일 때만 가능.
     *
     * @param id 구독 ID (path)
     * @return 200 OK
     */
    @PatchMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<Void>> resume(@PathVariable Long id) {
        String username = getCurrentUsername();
        subscriptionService.resume(id, username);
        return ResponseEntity.ok(ApiResponse.success("구독이 재개되었습니다."));
    }

    /**
     * API-SUB-005: 구독 해지 요청.
     * 다음 결제일 기준 해지 정책에 따라 해지 예정(CANCELLATION_PENDING)으로 전환한다.
     * 본인 구독이며 ACTIVE 또는 PAUSED 상태일 때만 가능.
     *
     * @param id     구독 ID (path)
     * @param reason 해지 사유 (선택, query param)
     * @return 200 OK
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        String username = getCurrentUsername();
        subscriptionService.cancel(id, username, reason);
        return ResponseEntity.ok(ApiResponse.success("구독 해지가 요청되었습니다."));
    }

    /**
     * 해지 예정 취소 (UC-C10 5-a). 해지 예정 상태를 유지(ACTIVE)로 되돌린다.
     * 본인 구독이며 CANCELLATION_PENDING 상태일 때만 가능.
     *
     * @param id 구독 ID (path)
     * @return 200 OK
     */
    @PatchMapping("/{id}/cancellation/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelCancellation(@PathVariable Long id) {
        String username = getCurrentUsername();
        subscriptionService.cancelCancellation(id, username);
        return ResponseEntity.ok(ApiResponse.success("구독 해지가 취소되었습니다. 계속해서 혜택을 누리실 수 있습니다."));
    }

    /**
     * SecurityContext에서 현재 로그인한 사용자 식별자(이메일)를 반환한다.
     *
     * @return 사용자 식별자 (이메일)
     * @throws BusinessException 인증되지 않은 경우 (SUBSCRIPTION_NOT_FOUND)
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getUser().getEmail();
        }
        if (principal instanceof String) {
            return (String) principal;
        }
        throw new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND);
    }
}
