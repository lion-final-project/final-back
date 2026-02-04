package com.example.finalproject.content.controller;

import com.example.finalproject.content.dto.request.PatchNoticeUpdateRequest;
import com.example.finalproject.content.dto.request.PostNoticeCreateRequest;
import com.example.finalproject.content.dto.response.GetNoticeResponse;
import com.example.finalproject.content.dto.response.PatchNoticeUpdateResponse;
import com.example.finalproject.content.dto.response.PostNoticeCreateResponse;
import com.example.finalproject.content.service.NoticeService;
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
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetNoticeResponse>>> getNotices(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GetNoticeResponse> response = noticeService.getNotices(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공지사항 등록
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostNoticeCreateResponse>> createNotice(
            @Valid @RequestBody PostNoticeCreateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PostNoticeCreateResponse response = noticeService.createNotice(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 공지사항 수정
     */
    @PatchMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<PatchNoticeUpdateResponse>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody PatchNoticeUpdateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        PatchNoticeUpdateResponse response = noticeService.updateNotice(noticeId, request, email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공지사항 삭제
     */
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Object>> deleteNotice(
            @PathVariable Long noticeId,
            Authentication authentication) {
        String email = authentication.getName();
        noticeService.deleteNotice(noticeId, email);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "공지사항이 삭제되었습니다.")));
    }
}