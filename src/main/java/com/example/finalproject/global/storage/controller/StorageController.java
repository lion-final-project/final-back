package com.example.finalproject.global.storage.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.global.storage.service.StorageService;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping("/{userId}/{applicantType}/{documentType}")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @PathVariable("userId") Long userId,
            @PathVariable("applicantType") ApplicantType applicantType,
            @PathVariable("documentType") DocumentType documentType,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("File upload : {}, applicantType: {}, documentType: {}",
                userId, applicantType, documentType);

        String fileUrl = storageService.uploadDocument(file, userId, applicantType, documentType);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("파일 업로드 성공", fileUrl));
    }
}
