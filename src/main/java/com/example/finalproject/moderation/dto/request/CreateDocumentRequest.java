package com.example.finalproject.moderation.dto.request;

import com.example.finalproject.moderation.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CreateDocumentRequest {

    @NotNull(message = "서류 유형은 필수입니다.")
    private DocumentType documentType;

    @NotBlank(message = "서류 URL은 필수입니다.")
    private String documentUrl;
//    document/
//     └── {userId}/           # 사용자 식별 (보안 및 관리 중심)
//            ├── store/          # 가게 점주 관련 서류 (ApplicantType 사용)
//            │     └── LICENSE.pdf # 파일명은 UUID_DocumentType.확장자 사용
//            └── rider/          # 라이더 관련 서류
//                  └── ID_CARD.png
}