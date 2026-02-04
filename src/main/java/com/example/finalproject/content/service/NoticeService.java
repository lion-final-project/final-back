package com.example.finalproject.content.service;

import com.example.finalproject.content.domain.Notice;
import com.example.finalproject.content.dto.request.PatchNoticeUpdateRequest;
import com.example.finalproject.content.dto.request.PostNoticeCreateRequest;
import com.example.finalproject.content.dto.response.GetNoticeDetailResponse;
import com.example.finalproject.content.dto.response.GetNoticeResponse;
import com.example.finalproject.content.dto.response.PatchNoticeUpdateResponse;
import com.example.finalproject.content.dto.response.PostNoticeCreateResponse;
import com.example.finalproject.content.repository.NoticeRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public Page<GetNoticeResponse> getNotices(Pageable pageable) {
        return noticeRepository.findAll(pageable)
                .map(GetNoticeResponse::from);
    }

    public Page<GetNoticeDetailResponse> getNoticesWithContent(Pageable pageable) {
        return noticeRepository.findAll(pageable)
                .map(GetNoticeDetailResponse::from);
    }

    @Transactional
    public PostNoticeCreateResponse createNotice(PostNoticeCreateRequest request, String email) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .build();

        Notice saved = noticeRepository.save(notice);
        return PostNoticeCreateResponse.from(saved);
    }

    @Transactional
    public PatchNoticeUpdateResponse updateNotice(Long noticeId, PatchNoticeUpdateRequest request, String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        notice.update(request.getTitle(), request.getContent());
        return PatchNoticeUpdateResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId, String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        noticeRepository.delete(notice);
    }
}
