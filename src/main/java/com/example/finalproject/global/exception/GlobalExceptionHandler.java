package com.example.finalproject.global.exception;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("BusinessException: {} - {}", errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        List<ApiResponse.FieldErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiResponse.FieldErrorDetail(
                        err.getField(),
                        err.getDefaultMessage()))
                .collect(Collectors.toList());
        ErrorCode ec = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity
                .status(ec.getStatus())
                .body(ApiResponse.fail(ec.getApiCode(), ec.getMessage(), details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(Exception e) {
        log.warn("HttpMessageNotReadableException", e);
        ErrorCode ec = ErrorCode.INVALID_JSON_FORMAT;
        return ResponseEntity.status(ec.getStatus()).body(ApiResponse.fail(ec));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(Exception e) {
        ErrorCode ec = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), "요청 파라미터 타입이 올바르지 않습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());
        ErrorCode ec = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException e) {
        log.warn("IllegalStateException: {}", e.getMessage());
        ErrorCode ec = ErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity.status(ec.getStatus())
                .body(ApiResponse.fail(ec.getCode(), e.getMessage()));
    }

    // 핸들러/리소스 없음 (404) 인증 확인용 URL 사용
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("NoResourceFound: {}", e.getResourcePath());
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.fail(ErrorCode.NOT_FOUND));
    }

    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    //     log.error("Unhandled Exception", e);
    //     return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
    //             .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    // }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {

        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            // 정상적인 SSE 종료 계열
            if (e instanceof IOException) {
                log.debug("[SSE] client disconnected: {}", e.getMessage());
            } else {
                log.error("[SSE] unexpected exception", e);
            }
            return ResponseEntity.noContent().build();
        }

        log.error("Unhandled Exception: {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
