package com.example.finalproject.content.service;

import com.example.finalproject.content.domain.Banner;
import com.example.finalproject.content.dto.request.PatchBannerUpdateRequest;
import com.example.finalproject.content.dto.request.PostBannerCreateRequest;
import com.example.finalproject.content.dto.response.GetBannerResponse;
import com.example.finalproject.content.dto.response.PatchBannerUpdateResponse;
import com.example.finalproject.content.dto.response.PostBannerCreateResponse;
import com.example.finalproject.content.enums.ContentStatus;
import com.example.finalproject.content.repository.BannerRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BannerService {

    private final BannerRepository bannerRepository;
    private final UserRepository userRepository;

    public List<GetBannerResponse> getBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAscIdAsc().stream()
                .map(GetBannerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostBannerCreateResponse createBanner(PostBannerCreateRequest request, String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Banner banner = Banner.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .linkUrl(request.getLinkUrl())
                .backgroundColor(request.getBackgroundColor())
                .displayOrder(request.getDisplayOrder())
                .status(request.getStatus() != null ? request.getStatus() : ContentStatus.ACTIVE)
                .startedAt(request.getStartedAt())
                .endedAt(request.getEndedAt())
                .build();

        Banner saved = bannerRepository.save(banner);
        return PostBannerCreateResponse.from(saved);
    }

    @Transactional
    public PatchBannerUpdateResponse updateBanner(Long bannerId, PatchBannerUpdateRequest request, String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BANNER_NOT_FOUND));

        banner.update(
                request.getTitle(),
                request.getContent(),
                request.getImageUrl(),
                request.getLinkUrl(),
                request.getBackgroundColor(),
                request.getDisplayOrder(),
                request.getStatus(),
                request.getStartedAt(),
                request.getEndedAt()
        );
        return PatchBannerUpdateResponse.from(banner);
    }

    @Transactional
    public void deleteBanner(Long bannerId, String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BANNER_NOT_FOUND));

        bannerRepository.delete(banner);
    }
}
