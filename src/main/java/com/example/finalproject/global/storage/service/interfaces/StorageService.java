package com.example.finalproject.global.storage.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

//DocumentStorageService
public interface StorageService {
    /**
     * 파일을 지정된 경로에 업로드
     * @param file 업로드할 파일
     * @param directoryPath 저장할 디렉토리 경로
     * @return 업로드된 파일의 전체 URL
     */
    String upload(MultipartFile file, String directoryPath);

    /**
     * 파일 삭제
     * @param fileUrl 삭제할 파일의 URL ex)http://localhost:9000/market-bucket/12/store/file.png
     */
    void delete(String fileUrl);
}
