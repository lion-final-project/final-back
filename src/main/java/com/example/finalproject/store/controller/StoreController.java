package com.example.finalproject.store.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.dto.request.PostStoreRegistrationRequest;
import com.example.finalproject.store.dto.response.GetMyStoreResponse;
import com.example.finalproject.store.dto.response.StoreRegistrationResponse;
import com.example.finalproject.store.service.StoreService;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    //todo: 유저 기능 개발시 삭제
    private final UserRepository userRepository;
    /*
    카테고리
    상호명
    매장주소 (좌표 정보 포함)
    대표자명 + 대표자 연락처, 마트 연락처
    마트소개
    마트대표 사진
    사업자명 + 사업자 등록번호, 사업자 등록증 첨부
    통신판매업 신고번호, 통신판매업 신고증 첨부
    정산 계좌(은행명, 계좌번호,예금주) + 통장 사본 첨부
    정기 휴무 + 운영 시간
     */

    @PostMapping("/registration")
    public ResponseEntity<ApiResponse<StoreRegistrationResponse>> registerStore(@Valid @RequestBody PostStoreRegistrationRequest request) {
        
        //todo: 유저 기능 완성 시 교체 할 것.
        User user;
        user = userRepository.findById(1L).get();

        StoreRegistrationResponse response = storeService.registerStore(user,request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("입점 신청이 완료되었습니다.", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<GetMyStoreResponse>> getMyStore(Authentication authentication) {
        String userName = authentication != null ? authentication.getName() : null;
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        GetMyStoreResponse response = storeService.getMyStore(userName);
        return ResponseEntity.ok(ApiResponse.success("내 상점 정보 조회가 완료되었습니다.", response));
    }

}
