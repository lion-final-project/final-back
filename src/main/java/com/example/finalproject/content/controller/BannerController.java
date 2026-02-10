package com.example.finalproject.content.controller;

import com.example.finalproject.content.dto.request.PatchBannerUpdateRequest;
import com.example.finalproject.content.dto.request.PostBannerCreateRequest;
import com.example.finalproject.content.dto.response.GetBannerResponse;
import com.example.finalproject.content.dto.response.PatchBannerUpdateResponse;
import com.example.finalproject.content.dto.response.PostBannerCreateResponse;
import com.example.finalproject.content.service.BannerService;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 배너 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetBannerResponse>>> getBanners() {
        List<GetBannerResponse> response = bannerService.getBanners();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 배너 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostBannerCreateResponse>> createBanner(
            @Valid @RequestBody PostBannerCreateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PostBannerCreateResponse response = bannerService.createBanner(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 배너 수정
     */
    @PatchMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<PatchBannerUpdateResponse>> updateBanner(
            @PathVariable Long bannerId,
            @RequestBody PatchBannerUpdateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PatchBannerUpdateResponse response = bannerService.updateBanner(bannerId, request, email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 배너 삭제
     */
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<ApiResponse<Object>> deleteBanner(
            @PathVariable Long bannerId,
            Authentication authentication) {
        String email = authentication.getName();
        bannerService.deleteBanner(bannerId, email);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "배너가 삭제되었습니다.")));
    }
}
