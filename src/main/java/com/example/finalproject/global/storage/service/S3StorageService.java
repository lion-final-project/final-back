package com.example.finalproject.global.storage.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.storage.service.interfaces.StorageService;
import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Primary
public class S3StorageService implements StorageService {

    private static final List<String> ALLOWED_EXTENSIONS = java.util.List.of("jpg", "jpeg", "png", "pdf");

    private final S3Template s3Template;
    private final String bucket;

    public S3StorageService(S3Template s3Template, @Value("${custom.s3.bucket}") String bucket) {
        this.s3Template = s3Template;
        this.bucket = bucket;
    }

    @Override
    public String upload(MultipartFile file, String directoryPath) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        // UUID 파일명 생성 (덮어쓰기 방지)
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // 경로 정규화 (앞뒤 슬래시 제거 및 이중 슬래시 방지)
        String safeDirectoryPath = StringUtils.cleanPath(directoryPath);
        if (safeDirectoryPath.startsWith("/")) {
            safeDirectoryPath = safeDirectoryPath.substring(1);
        }
        if (safeDirectoryPath.endsWith("/")) {
            safeDirectoryPath = safeDirectoryPath.substring(0, safeDirectoryPath.length() - 1);
        }

        String key = safeDirectoryPath + "/" + uniqueFilename;

        try (InputStream inputStream = file.getInputStream()) {
            return s3Template.upload(bucket, key, inputStream)
                    .getURL()
                    .toString();
        } catch (IOException e) {
            log.error("S3 Upload Failed: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

//    @Override
//    public String uploadDocument(MultipartFile file, Long userId, ApplicantType applicantType, DocumentType documentType) {
//        String directoryPath = String.format("document/%d/%s/%s",
//                userId,
//                applicantType.name().toLowerCase(),
//                documentType.name().toLowerCase()
//        );
//
//        log.info("StorageService.uploadDocument() userId: {}, path: {}", userId, directoryPath);
//        return upload(file, directoryPath);
//    }


    @Override
    public void delete(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        log.info("Deleting File. Bucket: {}, Key: {}", bucket, key);
        s3Template.deleteObject(bucket, key);
    }

    @Override
    public byte[] downloadByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return null;
        String key = extractKeyFromUrl(fileUrl);
        try {
            var resource = s3Template.download(bucket, key);
            try (InputStream is = resource.getInputStream()) {
                return is.readAllBytes();
            }
        } catch (IOException | S3Exception e) {
            log.warn("S3 download failed. url={}, key={}, error={}", fileUrl, key, e.getMessage());
            return null;
        }
    }

    /**
     * 전체 경로에서 kye추출
     * @return kye추출 ex)http://localhost:9000/market-bucket/12/store/file.png -> 12/store/file.png
     */
    private String extractKeyFromUrl(String url) {
        try {
            // 주소 파싱
            URI uri = new URI(url);
            String path = uri.getPath(); // 주소 파싱(URI 추출)

            // 주소 안에 버킷 이름이 포함되어있음 자름(MinIO 방식)
            String bucketPath = "/" + bucket + "/";
            if (path.startsWith(bucketPath)) {
                return path.substring(bucketPath.length());
            }

            // AWS S3는 도메인명에 버킷이름 있음
            if (path.startsWith("/")) {
                return path.substring(1);
            }

            return path;
        } catch (Exception e) {
            // 어짜피 delete()에서 에러남
            log.error("Failed to parse S3 URL: {}", url);
            return url;
        }
    }
}
