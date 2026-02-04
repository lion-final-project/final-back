package com.example.finalproject.global.exception.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// COMMON
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-000", "서버 내부 오류가 발생했습니다."),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "입력값이 유효하지 않습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-003", "지원하지 않는 HTTP 메서드입니다."),
	INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "COMMON-004", "요청 본문(JSON) 형식이 올바르지 않습니다."),

    // STORE
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "STORE-001", "이미 등록된 사업자등록번호입니다."),
    ALREADY_REGISTERED_STORE(HttpStatus.CONFLICT, "STORE-002", "이미 입점 신청한 사용자입니다."),
    PENDING_APPROVAL_EXISTS(HttpStatus.CONFLICT, "STORE-003", "이미 승인 대기중인 신청이 있습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-004", "상점을 찾을 수 없습니다."),
    INVALID_BUSINESS_HOUR(HttpStatus.BAD_REQUEST, "STORE-005", "운영 시간 정보가 올바르지 않습니다."),
    STORE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE-006", "상점 카테고리를 찾을 수 없습니다."),
    DUPLICATE_TELECOM_SALES_NUMBER(HttpStatus.CONFLICT, "STORE-007", "이미 등록된 통신판매업 신고번호입니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-005", "리소스를 찾을 수 없습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-006", "접근 권한이 없습니다."),

	// AUTH
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요합니다."),
	DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH-002", "이미 사용 중인 이메일입니다."),
	DUPLICATE_PHONE(HttpStatus.CONFLICT, "AUTH-003", "이미 가입된 휴대폰 번호입니다."),
	PHONE_VERIFICATION_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH-004", "휴대폰 인증이 필요합니다."),
	PHONE_VERIFICATION_ALREADY_USED(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH-005", "이미 사용된 인증 토큰입니다."),
	PHONE_VERIFICATION_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH-006", "인증이 만료되었습니다. 다시 시도해주세요."),
	PHONE_VERIFICATION_RESEND_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "AUTH-007", "재발송 횟수를 초과했습니다. 나중에 다시 시도해주세요."),
	PHONE_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH-008", "인증 요청을 찾을 수 없습니다. 인증번호를 먼저 요청해주세요."),
	PHONE_VERIFICATION_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-009", "인증번호가 일치하지 않습니다."),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-010", "유효하지 않거나 만료된 refresh token입니다."),
	EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-011", "해당 이메일을 찾을 수 없습니다."),
	PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH-012", "비밀번호가 일치하지 않습니다."),
	USER_STATUS_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-013", "정지되었거나 비활성화된 계정입니다."),
	REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH-014", "refresh token이 필요합니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-015", "사용자를 찾을 수 없습니다."),

	// STORAGE
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "STORAGE-002", "지원하지 않는 파일 형식입니다."),

	// NOTICE
	NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-001", "공지사항을 찾을 수 없습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}
