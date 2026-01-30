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

    // document/rider/{id} /idCord, /bankbook
    // document/store/{id} /idCord, /bankbook
}