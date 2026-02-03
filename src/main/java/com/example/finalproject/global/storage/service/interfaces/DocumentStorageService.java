package com.example.finalproject.global.storage.service.interfaces;

import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService extends StorageService {
    /**
     * 증빙 서류 업로드
     * @param file 업로드할 파일
     * @param userId 사용자 ID
     * @param applicantType 신청자 유형
     * @param documentType 서류 유형
     * @return 업로드된 파일의 전체 URL
     */
    String uploadDocument(MultipartFile file, Long userId, ApplicantType applicantType, DocumentType documentType);

//    document/
//     └── {userId}/           # 사용자 식별 (보안 및 관리 중심)
//            ├── store/          # 가게 점주 관련 서류 (ApplicantType 사용)
//            │     └── LICENSE.pdf # 파일명은 UUID_DocumentType.확장자 사용
//            └── rider/          # 라이더 관련 서류
//                  └── ID_CARD.png
}
