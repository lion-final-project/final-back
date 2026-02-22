package com.example.finalproject.content.controller;

import com.example.finalproject.content.dto.response.GetFaqDetailResponse;
import com.example.finalproject.content.service.FaqService;
import com.example.finalproject.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class CustomerFaqController {

    private final FaqService faqService;

    /**
     * 고객용 FAQ 목록 조회 (answer 포함)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetFaqDetailResponse>>> getFaqsForCustomer(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GetFaqDetailResponse> response = faqService.getFaqsWithAnswer(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
