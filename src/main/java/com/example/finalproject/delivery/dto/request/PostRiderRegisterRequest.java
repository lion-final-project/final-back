package com.example.finalproject.delivery.dto.request;

import com.example.finalproject.moderation.dto.request.CreateApprovalRequest;
import com.example.finalproject.moderation.dto.request.CreateDocumentRequest;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

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
    @JsonProperty("bank-name")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수 입력 값입니다.")
    @JsonProperty("bank-account")
    private String bankAccount;

    @NotBlank(message = "예금주명은 필수 입력 값입니다.")
    @JsonProperty("account-holder")
    private String accountHolder;

    // 증빙 서류 이미지 URL (S3/MinIO 업로드 후 전달받은 경로)
    @NotBlank(message = "신분증 이미지는 필수 입력 값입니다.")
    @JsonProperty("id-card-image")
    private String idCardImage;

    @NotBlank(message = "통장 사본 이미지는 필수 입력 값입니다.")
    @JsonProperty("bankbook-image")
    private String bankbookImage;
}
