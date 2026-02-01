package com.example.finalproject.global.exception.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// COMMON
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-002", "요청 값이 올바르지 않습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-003", "지원하지 않는 HTTP 메서드입니다."),
	INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "COMMON-004", "요청 본문(JSON) 형식이 올바르지 않습니다."),

	// STORAGE
	FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-001", "파일 업로드에 실패했습니다."),
	INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "STORAGE-002", "지원하지 않는 파일 형식입니다."),

    // STORE
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "STORE-001", "이미 등록된 사업자등록번호입니다."),
    ALREADY_REGISTERED_STORE(HttpStatus.CONFLICT, "STORE-002", "이미 입점 신청한 사용자입니다."),
    PENDING_APPROVAL_EXISTS(HttpStatus.CONFLICT, "STORE-003", "이미 승인 대기중인 신청이 있습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-004", "상점을 찾을 수 없습니다."),
    INVALID_BUSINESS_HOUR(HttpStatus.BAD_REQUEST, "STORE-005", "운영 시간 정보가 올바르지 않습니다."),
    STORE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-006", "상점 카테고리를 찾을 수 없습니다."),
    DUPLICATE_TELECOM_SALES_NUMBER(HttpStatus.CONFLICT, "STORE-007", "이미 등록된 통신판매업 신고번호입니다."),
    ;

	private final HttpStatus status;
	private final String code;
	private final String message;
}

