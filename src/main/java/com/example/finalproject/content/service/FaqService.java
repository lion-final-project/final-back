package com.example.finalproject.content.service;

import com.example.finalproject.content.domain.Faq;
import com.example.finalproject.content.dto.request.PatchFaqUpdateRequest;
import com.example.finalproject.content.dto.request.PostFaqCreateRequest;
import com.example.finalproject.content.dto.response.GetFaqDetailResponse;
import com.example.finalproject.content.dto.response.GetFaqResponse;
import com.example.finalproject.content.dto.response.PatchFaqUpdateResponse;
import com.example.finalproject.content.dto.response.PostFaqCreateResponse;
import com.example.finalproject.content.repository.FaqRepository;
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
public class FaqService {

    private final FaqRepository faqRepository;
    private final UserRepository userRepository;

    public Page<GetFaqResponse> getFaqs(Pageable pageable) {
        return faqRepository.findAll(pageable)
                .map(GetFaqResponse::from);
    }

    public Page<GetFaqDetailResponse> getFaqsWithAnswer(Pageable pageable) {
        return faqRepository.findAll(pageable)
                .map(GetFaqDetailResponse::from);
    }

    @Transactional
    public PostFaqCreateResponse createFaq(PostFaqCreateRequest request, User user) {
        // TODO: JWT 구현 후 관리자 권한 확인 로직 추가
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User author = userRepository.getReferenceById(user.getId());

        Faq faq = Faq.builder()
                .question(request.getQuestion())
                .answer(request.getAnswer())
                .author(author)
                .build();

        Faq saved = faqRepository.save(faq);
        return PostFaqCreateResponse.from(saved);
    }

    @Transactional
    public PatchFaqUpdateResponse updateFaq(Long faqId, PatchFaqUpdateRequest request, User user) {
        // TODO: JWT 구현 후 관리자 권한 확인 로직 추가
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAQ_NOT_FOUND));

        faq.update(request.getQuestion(), request.getAnswer());
        return PatchFaqUpdateResponse.from(faq);
    }

    @Transactional
    public void deleteFaq(Long faqId, User user) {
        // TODO: JWT 구현 후 관리자 권한 확인 로직 추가
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAQ_NOT_FOUND));

        faqRepository.delete(faq);
    }
}
