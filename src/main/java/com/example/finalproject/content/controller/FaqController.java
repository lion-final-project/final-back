package com.example.finalproject.content.controller;

import com.example.finalproject.content.dto.request.PatchFaqUpdateRequest;
import com.example.finalproject.content.dto.request.PostFaqCreateRequest;
import com.example.finalproject.content.dto.response.GetFaqResponse;
import com.example.finalproject.content.dto.response.PatchFaqUpdateResponse;
import com.example.finalproject.content.dto.response.PostFaqCreateResponse;
import com.example.finalproject.content.service.FaqService;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    /**
     * FAQ 조회 (관리자)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetFaqResponse>>> getFaqs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GetFaqResponse> response = faqService.getFaqs(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * FAQ 등록
     * TODO
     *  1. Security 적용시 @AuthenticationPrincipal 로 인증된 사용자 정보 받기
     *  2. 어노테이션 추가
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostFaqCreateResponse>> createFaq(
            @Valid @RequestBody PostFaqCreateRequest request,
            User user) {
        PostFaqCreateResponse response = faqService.createFaq(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * FAQ 수정
     * TODO
     *  1. Security 적용시 @AuthenticationPrincipal 로 인증된 사용자 정보 받기
     *  2. 어노테이션 추가
     */
    @PatchMapping("/{faqId}")
    public ResponseEntity<ApiResponse<PatchFaqUpdateResponse>> updateFaq(
            @PathVariable Long faqId,
            @RequestBody PatchFaqUpdateRequest request,
            User user) {
        PatchFaqUpdateResponse response = faqService.updateFaq(faqId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * FAQ 삭제
     * TODO
     *  1. Security 적용시 @AuthenticationPrincipal 로 인증된 사용자 정보 받기
     *  2. 어노테이션 추가
     */
    @DeleteMapping("/{faqId}")
    public ResponseEntity<ApiResponse<Object>> deleteFaq(
            @PathVariable Long faqId,
            User user) {
        faqService.deleteFaq(faqId, user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "FAQ가 삭제되었습니다.")));
    }
}
