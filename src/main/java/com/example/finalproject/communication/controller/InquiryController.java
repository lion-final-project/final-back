package com.example.finalproject.communication.controller;


import com.example.finalproject.communication.dto.request.PostInquiryAnswerRequest;
import com.example.finalproject.communication.dto.request.PostInquiryCreateRequest;
import com.example.finalproject.communication.dto.response.GetAdminInquiryDetailResponse;
import com.example.finalproject.communication.dto.response.GetAdminIquiriesResponse;
import com.example.finalproject.communication.dto.response.GetInquiriesResponse;
import com.example.finalproject.communication.dto.response.GetInquiryResponse;
import com.example.finalproject.communication.enums.InquiryStatus;
import com.example.finalproject.communication.service.InquiryService;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GetInquiryResponse>> create(
            //@AuthenticationPrincipal Authentication authentication,
            @Valid @RequestPart("request") PostInquiryCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        // Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;
        GetInquiryResponse getInquiryResponse = inquiryService.create(userId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("문의 접수가 완료되었습니다.", getInquiryResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetInquiriesResponse>>> myList(
            //@AuthenticationPrincipal Authentication authentication,
            @PageableDefault(
                    size = 5,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        // Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;

        Page<GetInquiriesResponse> myInquiryList = inquiryService.getMyList(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(myInquiryList));
    }


    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            //@AuthenticationPrincipal Authentication authentication,
            @PathVariable Long inquiryId) {

        // Long userId = (Long) authentication.getPrincipal();
        Long userId = 1L;
        inquiryService.deleteMyInquiry(userId, inquiryId);
        return ResponseEntity.ok(ApiResponse.success("문의가 삭제되었습니다."));
    }

    @GetMapping("/admin")
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

    @GetMapping("/admin/{inquiryId}")
    public ResponseEntity<ApiResponse<GetAdminInquiryDetailResponse>> adminDetail(
            @PathVariable Long inquiryId) {

        GetAdminInquiryDetailResponse adminInquiryDetail = inquiryService.getAdminInquiryDetail(inquiryId);

        return ResponseEntity.ok(ApiResponse.success(adminInquiryDetail));
    }

    @PostMapping("/admin/{inquiryId}/answer")
    public ResponseEntity<ApiResponse<Void>> answerInquiry(
            //@AuthenticationPrincipal Authentication authentication,
            @PathVariable Long inquiryId,
            @Valid @RequestBody PostInquiryAnswerRequest request) {
        // Long adminId = (Long) authentication.getPrincipal();
        Long adminId = 1L; // 임시 관리자

        inquiryService.answerInquiry(inquiryId, adminId, request);

        return ResponseEntity.ok(ApiResponse.success("문의 답변이 등록되었습니다."));
    }
}
