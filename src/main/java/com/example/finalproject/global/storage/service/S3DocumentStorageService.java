package com.example.finalproject.global.storage.service;

import com.example.finalproject.global.storage.service.interfaces.DocumentStorageService;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class S3DocumentStorageService extends S3StorageService implements DocumentStorageService {

    public S3DocumentStorageService(S3Template s3Template, @Value("${custom.s3.bucket}") String bucket) {
        super(s3Template, bucket);
    }


    @Override
    public String uploadDocument(MultipartFile file, Long userId, ApplicantType applicantType, DocumentType documentType) {
        String directoryPath = String.format("document/%d/%s/%s",
                userId,
                applicantType.name().toLowerCase(),
                documentType.name().toLowerCase()
        );

        log.info("StorageService.uploadDocument() userId: {}, path: {}", userId, directoryPath);
        return super.upload(file, directoryPath);
    }
}
