package com.example.finalproject.moderation.dto.request;

import com.example.finalproject.moderation.domain.Approval;
import com.example.finalproject.moderation.domain.ApprovalDocument;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.user.domain.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CreateApprovalRequest {

    @NotNull(message = "신청자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "신청자 유형은 필수입니다.")
    private ApplicantType applicantType;

    @NotEmpty(message = "증빙 서류는 최소 1개 이상 제출해야 합니다.")
    @Valid
    private List<CreateDocumentRequest> documents;
}
