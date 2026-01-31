package com.example.finalproject.global.storage.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    /**
     * 파일을 지정된 경로에 업로드
     * @param file 업로드할 파일
     * @param directoryPath 저장할 디렉토리 경로
     * @return 업로드된 파일의 전체 URL
     */
    String upload(MultipartFile file, String directoryPath);

    /**
     * 증빙 서류 업로드
     * @param file 업로드할 파일
     * @param userId 사용자 ID
     * @param applicantType 신청자 유형
     * @param documentType 서류 유형
     * @return 업로드된 파일의 전체 URL
     */
    String uploadDocument(MultipartFile file, Long userId, ApplicantType applicantType, DocumentType documentType);

    /**
     * 파일 삭제
     * @param fileUrl 삭제할 파일의 URL ex)http://localhost:9000/market-bucket/12/store/file.png
     */
    void delete(String fileUrl);

//    document/
//     └── {userId}/           # 사용자 식별 (보안 및 관리 중심)
//            ├── store/          # 가게 점주 관련 서류 (ApplicantType 사용)
//            │     └── LICENSE.pdf # 파일명은 UUID_DocumentType.확장자 사용
//            └── rider/          # 라이더 관련 서류
//                  └── ID_CARD.png
}
