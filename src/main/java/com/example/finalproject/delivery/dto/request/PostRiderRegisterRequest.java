package com.example.finalproject.delivery.dto.request;

import com.example.finalproject.moderation.dto.request.CreateApprovalRequest;
import com.example.finalproject.moderation.dto.request.CreateDocumentRequest;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 라이더 등록 신청을 위한 요청 DTO
 * 사용자의 인적 사항, 정산 계좌 정보 및 증빙 서류 이미지를 포함합니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostRiderRegisterRequest {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotBlank(message = "연락처는 필수 입력 값입니다.")
    private String phone;

    // 정산 계좌 정보
    @NotBlank(message = "은행명은 필수 입력 값입니다.")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수 입력 값입니다.")
    private String bankAccount;

    @NotBlank(message = "예금주명은 필수 입력 값입니다.")
    private String accountHolder;

    // 증빙 서류 이미지 URL (S3/MinIO 업로드 후 전달받은 경로)
    @NotBlank(message = "신분증 이미지는 필수 입력 값입니다.")
    private String idCardImage;

    @NotBlank(message = "통장 사본 이미지는 필수 입력 값입니다.")
    private String bankbookImage;

    /**
     * moderation 도메인의 승인 요청 DTO로 변환하는 메서드
     * 유저 id는 따로 안받고 시큐리티 컨텍스트 사용해 가져옴
     *
     * @param userId 인증된 사용자의 식별자
     * @return moderation도메인 서비스에 전달할 승인 요청 객체
     */
    public CreateApprovalRequest toApprovalRequest(Long userId) {
        List<CreateDocumentRequest> documents = List.of(
                CreateDocumentRequest.builder()
                        .documentType(DocumentType.ID_CARD)
                        .documentUrl(idCardImage)
                        .build(),
                CreateDocumentRequest.builder()
                        .documentType(DocumentType.BANK_PASSBOOK)
                        .documentUrl(bankbookImage)
                        .build()
        );

        return CreateApprovalRequest.builder()
                .userId(userId)
                .applicantType(ApplicantType.RIDER)
                .documents(documents)
                .build();
    }
}
