package com.example.finalproject.coupon.controller;

import com.example.finalproject.coupon.dto.response.CouponResponse;
import com.example.finalproject.coupon.repository.CouponRepository;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

//쿠폰 관련 임시 구현

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;


    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAvailableCoupons(Authentication authentication) {
        Long userId = userRepository.findByEmail(authentication.getName())
                .map(u -> u.getId())
                .orElse(null);
        if (userId == null) {
            log.info("[쿠폰] 사용 가능 쿠폰 조회. 사용자 미인증 → 0건");
            return ResponseEntity.ok(ApiResponse.success("사용 가능한 쿠폰 목록", List.of()));
        }
        List<CouponResponse> list = couponRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(c -> CouponResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .discountAmount(c.getDiscountAmount())
                        .build())
                .toList();
        log.info("[쿠폰] 사용 가능 쿠폰 조회. 사용자={}, 쿠폰 수={}건, 쿠폰명={}",
                authentication.getName(), list.size(),
                list.isEmpty() ? "(없음)" : list.stream().map(CouponResponse::getName).toList());
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 쿠폰 목록", list));
    }
}
