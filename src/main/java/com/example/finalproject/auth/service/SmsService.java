package com.example.finalproject.auth.service;

import com.example.finalproject.auth.util.SmsUtil;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

//COOLSMS 예제 참고
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private static final String REDIS_KEY_AUTH = "SMS:AUTH:";
    private static final String REDIS_KEY_RESEND = "SMS:RESEND:";
    private static final String REDIS_KEY_VERIFIED = "SMS:VERIFIED:";
    /** 방법 1: 인증 완료 후 해당 번호를 TTL 동안 "인증됨" 상태로 보관 (교차검증/회원가입 유예) */
    private static final String REDIS_KEY_PHONE_VERIFIED = "SMS:PHONE_VERIFIED:";
    private static final int AUTH_TTL_MINUTES = 3;
    private static final int RESEND_SESSION_TTL_MINUTES = 10;
    private static final int VERIFIED_TOKEN_TTL_MINUTES = 10;
    /** 휴대폰 "인증 완료" 상태 유지 시간(초) — 이 시간 내에 회원가입 가능 */
    private static final int PHONE_VERIFIED_TTL_SECONDS = 300; // 5분
    private static final int RESEND_LIMIT = 5;

    private final DefaultMessageService messageService;
    private final StringRedisTemplate redisTemplate;
    private final SmsUtil smsUtil;

    @Value("${coolsms.sender}")
    private String senderNumber;


    public String sendAuthCode(String phoneNumber) {
        String resendKey = REDIS_KEY_RESEND + phoneNumber;
        Long resendCount = redisTemplate.opsForValue().increment(resendKey);
        if (resendCount != null && resendCount == 1) {
            redisTemplate.expire(resendKey, RESEND_SESSION_TTL_MINUTES, TimeUnit.MINUTES);
        }
        if (resendCount != null && resendCount > RESEND_LIMIT) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_RESEND_LIMIT);
        }

        String authCode = SmsUtil.generateAuthCode();
        String messageText = smsUtil.makeAuthMessage(authCode);

        Message message = new Message();
        message.setFrom(formatSenderNumber(senderNumber));
        message.setTo(formatPhoneNumber(phoneNumber));
        message.setText(messageText);

        try {
            SingleMessageSentResponse response = messageService.sendOne(
                    new SingleMessageSendingRequest(message));
            log.info("[SMS] 인증번호 발송 완료 - to: {}, code: {}, response: {}", phoneNumber, authCode, response);
        } catch (Exception e) {
            redisTemplate.opsForValue().decrement(resendKey);
            log.error("[SMS] 인증번호 발송 실패 - to: {}, message: {}", phoneNumber, e.getMessage(), e);
            throw new RuntimeException("SMS 발송에 실패했습니다. 발신번호·API 키 설정을 확인해 주세요.", e);
        }

        String authKey = REDIS_KEY_AUTH + phoneNumber;
        redisTemplate.opsForValue().set(authKey, authCode, AUTH_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("[SMS] 인증번호 Redis 저장 - key: {}, code: {}", authKey, authCode);
        return authCode;
    }

    //Redis에서 확인 후 1회 사용 삭제
    //성공 시 verificationToken 발급 Redis 저장 회원가입 시 사용
    public String verifyAuthCode(String phoneNumber, String inputCode) {
        String authKey = REDIS_KEY_AUTH + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(authKey);
        log.info("[SMS] 인증번호 검증 요청 - key: {}, 입력값: {}, 저장값: {}", authKey, inputCode, storedCode);

        if (storedCode == null) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_EXPIRED);
        }
        if (!storedCode.equals(inputCode)) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_MISMATCH);
        }

        redisTemplate.delete(authKey);
        redisTemplate.delete(REDIS_KEY_RESEND + phoneNumber);
        log.info("[SMS] 인증번호 검증 성공 - key: {}", authKey);

        String token = UUID.randomUUID().toString();
        String verifiedKey = REDIS_KEY_VERIFIED + token;
        redisTemplate.opsForValue().set(verifiedKey, phoneNumber, VERIFIED_TOKEN_TTL_MINUTES, TimeUnit.MINUTES);

        // 방법 1: 해당 번호를 TTL(5분) 동안 "인증 완료" 상태로 저장 → 회원가입 시 토큰 없이도 검증 가능
        String phoneVerifiedKey = REDIS_KEY_PHONE_VERIFIED + phoneNumber;
        redisTemplate.opsForValue().set(phoneVerifiedKey, "1", PHONE_VERIFIED_TTL_SECONDS, TimeUnit.SECONDS);

        log.info("[SMS] 인증 토큰 발급 - token: {}, phone: {}, phoneVerified TTL: {}s", token, phoneNumber, PHONE_VERIFIED_TTL_SECONDS);
        return token;
    }

    //Redis에서 phone 조회 후 1회 사용 삭제 (phoneVerificationToken 사용 시)
    public void validateAndConsumeVerificationToken(String phone, String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_REQUIRED);
        }
        String verifiedKey = REDIS_KEY_VERIFIED + token;
        String storedPhone = redisTemplate.opsForValue().get(verifiedKey);
        if (storedPhone == null) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_EXPIRED);
        }
        if (!storedPhone.equals(phone)) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_REQUIRED);
        }
        redisTemplate.delete(verifiedKey);
        redisTemplate.delete(REDIS_KEY_PHONE_VERIFIED + phone); // 동일 번호의 phone-verified도 소비
        log.info("[SMS] 인증 토큰 사용 완료 - phone: {}", phone);
    }

    /**
     * 방법 1: 인증 완료 후 TTL(5분) 동안 유지된 "휴대폰 인증 완료" 상태를 1회 사용 후 삭제.
     * 토큰 없이 회원가입 시 이 메서드로 검증 가능.
     */
    public void validateAndConsumePhoneVerified(String phone) {
        String key = REDIS_KEY_PHONE_VERIFIED + phone;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_EXPIRED);
        }
        redisTemplate.delete(key);
        log.info("[SMS] 휴대폰 인증 완료 상태 사용(소비) - phone: {}", phone);
    }

    //재발송 수 조회
    public int getRemainingResendAttempts(String phoneNumber) {
        String resendKey = REDIS_KEY_RESEND + phoneNumber;
        String countStr = redisTemplate.opsForValue().get(resendKey);
        if (countStr == null) {
            return RESEND_LIMIT;
        }
        int count = Integer.parseInt(countStr);
        return Math.max(0, RESEND_LIMIT - count);
    }

    private String formatPhoneNumber(String phone) {
        return phone != null ? phone.replaceAll("[^0-9]", "") : "";
    }

    private String formatSenderNumber(String sender) {
        return sender != null ? sender.replaceAll("[^0-9]", "") : "";
    }
}
