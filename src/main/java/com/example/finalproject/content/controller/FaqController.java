package com.example.finalproject.content.controller;

import com.example.finalproject.content.dto.request.PatchFaqUpdateRequest;
import com.example.finalproject.content.dto.request.PostFaqCreateRequest;
import com.example.finalproject.content.dto.response.GetFaqDetailResponse;
import com.example.finalproject.content.dto.response.PatchFaqUpdateResponse;
import com.example.finalproject.content.dto.response.PostFaqCreateResponse;
import com.example.finalproject.content.service.FaqService;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import java.util.Map;

@RestController
@RequestMapping("/api/admin/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    /**
     * FAQ 조회 (관리자) - answer 포함
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetFaqDetailResponse>>> getFaqs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GetFaqDetailResponse> response = faqService.getFaqsWithAnswer(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * FAQ 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostFaqCreateResponse>> createFaq(
            @Valid @RequestBody PostFaqCreateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PostFaqCreateResponse response = faqService.createFaq(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * FAQ 수정
     */
    @PatchMapping("/{faqId}")
    public ResponseEntity<ApiResponse<PatchFaqUpdateResponse>> updateFaq(
            @PathVariable Long faqId,
            @RequestBody PatchFaqUpdateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PatchFaqUpdateResponse response = faqService.updateFaq(faqId, request, email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * FAQ 삭제
     */
    @DeleteMapping("/{faqId}")
    public ResponseEntity<ApiResponse<Object>> deleteFaq(
            @PathVariable Long faqId,
            Authentication authentication) {
        String email = authentication.getName();
        faqService.deleteFaq(faqId, email);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "FAQ가 삭제되었습니다.")));
    }
}
