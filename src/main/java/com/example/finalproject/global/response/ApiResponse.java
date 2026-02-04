package com.example.finalproject.global.response;

import com.example.finalproject.global.exception.custom.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private static final DateTimeFormatter ISO_TIMESTAMP =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Asia/Seoul"));

    private boolean success;
    private String code;
    private String message;
    private T data;
    private ErrorBody error;
    private String timestamp;

    @Getter
    @AllArgsConstructor
    public static class ErrorBody { 
        private String code;
        private String message;
        private List<FieldErrorDetail> details;

        public static ErrorBody of(String code, String message) {
            return new ErrorBody(code, message, Collections.emptyList());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String message;
    }

    private static String nowTimestamp() {
        return ISO_TIMESTAMP.format(Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) { //성공 응답
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data, null, nowTimestamp());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data, null, nowTimestamp());
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", null, null, nowTimestamp());
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, "SUCCESS", message, null, null, nowTimestamp());
    }

    
    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, null, null, ErrorBody.of(code, message), nowTimestamp());
    }

    public static <T> ApiResponse<T> fail(String code, String message, List<FieldErrorDetail> details) { //에러 응답
        return new ApiResponse<>(
                false, null, null, null,
                new ErrorBody(code, message, details != null ? details : Collections.emptyList()),
                nowTimestamp());
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                false, null, null, null,
                ErrorBody.of(errorCode.getCode(), errorCode.getMessage()),
                nowTimestamp());
    }
}