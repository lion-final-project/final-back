package com.example.finalproject.content.controller;

import com.example.finalproject.content.dto.response.GetBannerResponse;
import com.example.finalproject.content.service.BannerService;
import com.example.finalproject.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class CustomerBannerController {

    private final BannerService bannerService;

    /**
     * 고객용 배너 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetBannerResponse>>> getBannersForCustomer() {
        List<GetBannerResponse> response = bannerService.getActiveBannersForCustomer();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

