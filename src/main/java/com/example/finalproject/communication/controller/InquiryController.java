package com.example.finalproject.communication.controller;


import com.example.finalproject.communication.dto.request.PostInquiryCreateRequest;
import com.example.finalproject.communication.dto.response.GetInquiriesResponse;
import com.example.finalproject.communication.dto.response.GetInquiryResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
            Authentication authentication,
            @Valid @RequestPart("request") PostInquiryCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {

        GetInquiryResponse getInquiryResponse = inquiryService.create(authentication.getName(), request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("문의 접수가 완료되었습니다.", getInquiryResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetInquiriesResponse>>> myList(
            Authentication authentication,
            @PageableDefault(
                    size = 5,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<GetInquiriesResponse> myInquiryList = inquiryService.getMyList(authentication.getName(), pageable);

        return ResponseEntity.ok(ApiResponse.success(myInquiryList));
    }


    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable Long inquiryId) {

        inquiryService.deleteMyInquiry(authentication.getName(), inquiryId);
        return ResponseEntity.ok(ApiResponse.success("문의가 삭제되었습니다."));
    }
}
