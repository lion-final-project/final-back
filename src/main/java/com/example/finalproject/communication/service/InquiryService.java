package com.example.finalproject.communication.service;


import com.example.finalproject.communication.domain.Inquiry;
import com.example.finalproject.communication.dto.request.PostInquiryAnswerRequest;
import com.example.finalproject.communication.dto.request.PostInquiryCreateRequest;
import com.example.finalproject.communication.dto.response.GetAdminInquiryDetailResponse;
import com.example.finalproject.communication.dto.response.GetAdminIquiriesResponse;
import com.example.finalproject.communication.dto.response.GetInquiriesResponse;
import com.example.finalproject.communication.dto.response.GetInquiryResponse;
import com.example.finalproject.communication.enums.InquiryStatus;
import com.example.finalproject.communication.enums.NotificationRefType;
import com.example.finalproject.communication.repository.InquiryRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.storage.service.interfaces.StorageService;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final StorageService s3StorageService;
    private final NotificationService notificationService;

    public GetInquiryResponse create(Long userId, PostInquiryCreateRequest req, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String fileUrl = s3StorageService.upload(file, "inquiries" + userId);

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .category(req.getCategory())
                .title(req.getTitle())
                .content(req.getContent())
                .fileUrl(fileUrl)
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return GetInquiryResponse.from(savedInquiry);
    }

    @Transactional(readOnly = true)
    public Page<GetInquiriesResponse> getMyList(Long userId, Pageable pageable) {
        return inquiryRepository.findAllByUserId(userId, pageable)
                .map(GetInquiriesResponse::from);
    }

    public void deleteMyInquiry(Long userId, Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findByIdAndUserId(inquiryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("문의가 없거나 접근할 수 없습니다."));

        if (inquiry.isAnswered()) {
            throw new IllegalStateException("답변 완료된 문의는 삭제할 수 없습니다.");
        }

        s3StorageService.delete(inquiry.getFileUrl());

        inquiryRepository.delete(inquiry);
    }

    public Page<GetAdminIquiriesResponse> getInquiryList(
            InquiryStatus status,
            Pageable pageable) {

        Page<Inquiry> page;

        if (status == null) {
            page = inquiryRepository.findAll(pageable);
        } else {
            page = inquiryRepository.findAllByStatus(status, pageable);
        }

        return page.map(GetAdminIquiriesResponse::from);
    }

    @Transactional(readOnly = true)
    public GetAdminInquiryDetailResponse getAdminInquiryDetail(Long inquiryId) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

        return GetAdminInquiryDetailResponse.from(inquiry);
    }

    @Transactional
    public void answerInquiry(
            Long inquiryId,
            Long adminId,
            PostInquiryAnswerRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

        if (inquiry.getStatus() == InquiryStatus.ANSWERED) {
            throw new BusinessException(ErrorCode.INQUIRY_ALREADY_ANSWERED);
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!admin.isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_AUTHORITY_REQUIRED);
        }

        inquiry.markAnswered(admin, request.getAnswer());

        notificationService.notifyUser(
                inquiry.getUser().getId(),
                "문의 답변 등록",
                "문의에 대한 답변이 등록되었습니다: " + inquiry.getTitle(),
                NotificationRefType.CUSTOMER
        );
    }
}
