package com.example.finalproject.global.storage.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.global.storage.enums.StoreImageType;
import com.example.finalproject.global.storage.service.interfaces.DocumentStorageService;
import com.example.finalproject.global.storage.service.interfaces.StorageService;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;
    private final DocumentStorageService documentStorageService;
    private final UserRepository userRepository;

    @PostMapping("/{userId}/{applicantType}/{documentType}")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @PathVariable("userId") Long userId,
            @PathVariable("applicantType") ApplicantType applicantType,
            @PathVariable("documentType") DocumentType documentType,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("File upload : {}, applicantType: {}, documentType: {}",
                userId, applicantType, documentType);

        String fileUrl = documentStorageService.uploadDocument(file, userId, applicantType, documentType);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("파일 업로드 성공", fileUrl));
    }

    /**
     * Store 이미지 업로드 (마트 대표 사진, 상품 이미지 등)
     * 경로: store/{userId}/{type}/UUID_파일명.jpg
     * @param file 이미지 파일
     * @param type 이미지 타입 (PROFILE: 마트 대표 사진, PRODUCT: 상품 이미지)
     */
    @PostMapping(value = "/store/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> uploadStoreImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "PROFILE") StoreImageType type,
            Authentication authentication) {

        //todo: Authentication authentication -> 나중에 userDetails 로 수정할 것.
        String userName = authentication.getName();
        User user = userRepository.findByEmail(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
        //=============================

        String directoryPath = String.format("store/%d/%s", user.getId(), type.getDirectory());
        log.info("Store image upload: userId={}, type={}, path={}", user.getId(), type, directoryPath);

        String fileUrl = storageService.upload(file, directoryPath);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("이미지 업로드 성공", fileUrl));
    }

    /**
     * S3에 올렸던 URL로 파일 다운로드 (바이트 반환)
     * @param url S3 파일 URL (쿼리 파라미터)
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadByUrl(@RequestParam("url") String url) {
        byte[] bytes = storageService.downloadByUrl(url);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        // Content-Disposition으로 파일 다운로드 유도 (파일명은 URL에서 추출 또는 기본값)
        String filename = "download";
        try {
            String path = new java.net.URI(url).getPath();
            if (path != null && path.contains("/")) {
                filename = path.substring(path.lastIndexOf('/') + 1);
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    /**
     * S3/MinIO URL을 브라우저에서 바로 미리보기할 수 있도록 프록시 응답
     * Content-Type을 파일 확장자로 추정해 inline 반환한다.
     */
    @GetMapping("/preview")
    public ResponseEntity<byte[]> previewByUrl(@RequestParam("url") String url) {
        byte[] bytes = storageService.downloadByUrl(url);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        String filename = "preview";
        try {
            String path = new java.net.URI(url).getPath();
            if (path != null && path.contains("/")) {
                filename = path.substring(path.lastIndexOf('/') + 1);
            }
        } catch (Exception ignored) {}

        MediaType mediaType = MediaTypeFactory.getMediaType(filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(bytes);
    }
}
