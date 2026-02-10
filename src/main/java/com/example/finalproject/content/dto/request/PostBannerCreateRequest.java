package com.example.finalproject.content.dto.request;

import com.example.finalproject.content.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostBannerCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String content;

    // 이미지 URL은 선택 입력 (URL 직접 입력 또는 파일 업로드 결과 사용)
    private String imageUrl;

    private String linkUrl;

    private String backgroundColor;

    private Integer displayOrder;

    private ContentStatus status;

    @NotNull(message = "노출 시작일시는 필수입니다.")
    private LocalDateTime startedAt;

    @NotNull(message = "노출 종료일시는 필수입니다.")
    private LocalDateTime endedAt;
}
