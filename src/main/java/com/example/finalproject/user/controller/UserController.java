package com.example.finalproject.user.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.dto.response.StoreNearbyResponse;
import com.example.finalproject.user.dto.request.GetStoreSearchRequest;
import com.example.finalproject.user.dto.response.GetMyProfileResponse;
import com.example.finalproject.user.dto.response.GetWithdrawalCheckResponse;
import com.example.finalproject.user.dto.response.PostWithdrawalConfirmResponse;
import com.example.finalproject.user.service.interfaces.UserService;
import com.example.finalproject.user.withdrawal.dto.BlockedReason;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 유저는 주소에 저장된 위도 경도 가져옴
     * 비회원은 카카오맵에서 핑찍은 뒤에 위도경도 가져옴
     */
    @GetMapping("/stores")
    public ResponseEntity<ApiResponse<Slice<StoreNearbyResponse>>> findNearbyStores(
            @ModelAttribute @Valid GetStoreSearchRequest request
    ){
        Slice<StoreNearbyResponse> response = userService.getNearbyStores(request);
        return ResponseEntity.ok(ApiResponse.success("마켓 조회 성공", response));
    }

    @GetMapping("/me/withdrawal/eligibility")
    public ResponseEntity<ApiResponse<GetWithdrawalCheckResponse>> checkWithdrawal(Authentication authentication) {
        GetWithdrawalCheckResponse response = userService.checkWithdrawalEligibility(authentication);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴 가능 여부 조회가 완료되었습니다.", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetMyProfileResponse>> getMyProfile(Authentication authentication) {
        GetMyProfileResponse response = userService.getMyProfile(authentication);
        return ResponseEntity.ok(ApiResponse.success("내 프로필 조회가 완료되었습니다.", response));
    }


    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<?>> withdraw(Authentication authentication) {
        GetWithdrawalCheckResponse check = userService.checkWithdrawalEligibility(authentication);
        if (!check.isCanWithdraw()) {
            List<ApiResponse.FieldErrorDetail> details = check.getBlockedReasons().stream()
                    .map(BlockedReason::getMessage)
                    .map(msg -> new ApiResponse.FieldErrorDetail("withdrawal", msg))
                    .toList();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("USER-001", "탈퇴가 제한되었습니다.", details));
        }
        PostWithdrawalConfirmResponse response = userService.withdraw(authentication);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴가 완료되었습니다.", response));
    }

}
