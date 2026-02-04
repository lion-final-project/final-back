package com.example.finalproject.content.controller;

import com.example.finalproject.content.dto.response.GetNoticeDetailResponse;
import com.example.finalproject.content.service.NoticeService;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class CustomerNoticeController {

    private final NoticeService noticeService;

    /**
     * 고객용 공지사항 목록 조회 (content 포함)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetNoticeDetailResponse>>> getNoticesForCustomer(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GetNoticeDetailResponse> response = noticeService.getNoticesWithContent(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
