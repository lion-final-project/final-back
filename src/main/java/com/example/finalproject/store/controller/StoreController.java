package com.example.finalproject.store.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.store.dto.request.PostStoreRegistrationRequest;
import com.example.finalproject.store.dto.response.GetStoreCategoryResponse;
import com.example.finalproject.store.dto.response.StoreRegistrationResponse;
import com.example.finalproject.store.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    /**
     * 마트 입점 신청
     * @param request 입점 신청 요청 정보 (카테고리, 상호명, 주소, 사업자 정보, 증빙 서류 URL 등)
     * @param authentication 현재 인증된 사용자 정보
     * @return 생성된 입점 신청 정보 (storeId, approvalId, status)
     */
    //todo: Authentication authentication -> 나중에 userDetails 로 수정할 것.
    @PostMapping("/registration")
    //@PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<StoreRegistrationResponse>> createStoreApplication(
            @Valid @RequestBody PostStoreRegistrationRequest request,
            Authentication authentication) {

        String userName = authentication.getName();

        StoreRegistrationResponse response = storeService.createStoreApplication(userName, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("입점 신청이 완료되었습니다.", response));
    }

    /**
     * 마트 카테고리 목록 조회
     * @return 전체 카테고리 목록 (과일/채소, 정육/계란, 수산/해산물 등)
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<GetStoreCategoryResponse>>> getStoreCategories() {
        List<GetStoreCategoryResponse> list = GetStoreCategoryResponse.listAll();
        return ResponseEntity.ok(ApiResponse.success("상점 카테고리 목록 조회가 완료되었습니다.", list));
    }

}
