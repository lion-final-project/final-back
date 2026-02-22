package com.example.finalproject.communication.controller;

import com.example.finalproject.communication.dto.request.PostInquiryAnswerRequest;
import com.example.finalproject.communication.dto.response.GetAdminInquiryDetailResponse;
import com.example.finalproject.communication.dto.response.GetAdminIquiriesResponse;
import com.example.finalproject.communication.enums.InquiryStatus;
import com.example.finalproject.communication.service.InquiryService;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetAdminIquiriesResponse>>> getInquiryList(
            @RequestParam(required = false) InquiryStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<GetAdminIquiriesResponse> inquiries = inquiryService.getInquiryList(status, pageable);

        return ResponseEntity.ok(ApiResponse.success(inquiries));
    }

    @GetMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<GetAdminInquiryDetailResponse>> adminDetail(@PathVariable Long inquiryId) {

        GetAdminInquiryDetailResponse adminInquiryDetail = inquiryService.getAdminInquiryDetail(inquiryId);

        return ResponseEntity.ok(ApiResponse.success(adminInquiryDetail));
    }

    @PostMapping("/{inquiryId}/answer")
    public ResponseEntity<ApiResponse<Void>> answerInquiry(
            Authentication authentication,
            @PathVariable Long inquiryId,
            @Valid @RequestBody PostInquiryAnswerRequest request) {

        inquiryService.answerInquiry(inquiryId, authentication.getName(), request);

        return ResponseEntity.ok(ApiResponse.success("문의 답변이 등록되었습니다."));
    }
}
