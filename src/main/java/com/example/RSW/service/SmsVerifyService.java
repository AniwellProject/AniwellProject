package com.example.RSW.service;

import com.example.RSW.vo.ResultData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsVerifyService {

    // === 설정값 (application.yml) ===
    @Value("${coolsms.api-key}")  private String apiKey;
    @Value("${coolsms.api-secret}") private String apiSecret;
    @Value("${coolsms.domain:https://api.solapi.com}") private String domain;
    @Value("${coolsms.from}") private String from; // 콘솔 승인된 발신번호 (숫자만 권장)
    @Value("${coolsms.ttl-seconds:180}") private int ttlSeconds;           // 인증코드 유효시간(초)
    @Value("${coolsms.cooldown-seconds:60}") private int cooldownSeconds;  // 재전송 쿨다운(초)

    // === 인프라 ===
    private final RedisTemplate<String, String> redis;
    private DefaultMessageService messageService;

    @PostConstruct
    void init() {
        messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, domain);
    }

    // === 키 규칙 ===
    private String keyCode(String phone)    { return "sms:verify:code:" + phone; }
    private String keyCooldown(String phone){ return "sms:verify:cooldown:" + phone; }

    // 숫자만 남기기
    private String normalize(String raw) { return raw == null ? "" : raw.replaceAll("\\D", ""); }

    // 코드 해시(원문 저장 안 함)
    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /** 인증코드 전송 */
    public ResultData<?> sendCode(String rawPhone) {
        final String to = normalize(rawPhone);
        if (to.isBlank()) return ResultData.from("F-PHONE", "전화번호를 입력하세요.");

        // 재전송 쿨다운 체크
        final String cdKey = keyCooldown(to);
        if (Boolean.TRUE.equals(redis.hasKey(cdKey))) {
            Long remain = redis.getExpire(cdKey, TimeUnit.SECONDS);
            long retryAfter = Math.max(1, remain == null ? 0 : remain);
            return ResultData.from("F-COOLDOWN", "잠시 후 다시 시도해 주세요.",
                    "retryAfterSec", retryAfter);
        }

        // 발신번호/형식 가드 (숫자만 10~11자리 국내 휴대폰 가정)
        final String fromClean = normalize(from);
        if (!fromClean.matches("^01\\d{8,9}$")) {
            log.warn("[SMS] invalid from number format: {}", from);
            return ResultData.from("F-FROM", "발신번호 형식이 올바르지 않습니다. (숫자만 10~11자리)");
        }

        // 6자리 코드 생성 + 해시 저장
        final String code = String.format("%06d", (int)(Math.random() * 1_000_000));
        final String hash = sha256(code + "|" + to);
        redis.opsForValue().set(keyCode(to), hash, ttlSeconds, TimeUnit.SECONDS);

        // 쿨다운 시작
        redis.opsForValue().set(cdKey, "1", cooldownSeconds, TimeUnit.SECONDS);

        // 문자 발송
        Message msg = new Message();
        msg.setFrom(fromClean);
        msg.setTo(to);
        int ttlMin = Math.max(1, (int)Math.ceil(ttlSeconds / 60.0));
        msg.setText("[Aniwell] 인증번호 " + code + " (유효 " + ttlMin + "분)");

        log.info("[SMS] try send from={} to={}", fromClean, to);

        try {
            messageService.send(msg);
            return ResultData.from("S-OK", "인증번호를 전송했습니다.",
                    "ttlSec", ttlSeconds,
                    "cooldownSec", cooldownSeconds);
        } catch (NurigoMessageNotReceivedException e) {
            String m = e.getMessage() == null ? "" : e.getMessage();
            log.warn("[SMS] send failed: {}", m);
            // 실패 시 보관 데이터 정리
            redis.delete(keyCode(to));
            redis.delete(cdKey);

            // 통신사 차단(3059/번호도용/변작) 매핑
            if (m.contains("3059") || m.contains("번호도용") || m.contains("변작")) {
                return ResultData.from("F-SENDER-BLOCK",
                        "발신번호가 통신사 정책에 따라 차단되었습니다. 승인된 발신번호(숫자만)로 설정 후 다시 시도해 주세요.");
            }
            return ResultData.from("F-SEND", "문자 발송에 실패했습니다: " + m);
        } catch (Exception e) {
            log.error("[SMS] system error", e);
            redis.delete(keyCode(to));
            redis.delete(cdKey);
            return ResultData.from("F-ERROR", "시스템 오류가 발생했습니다.");
        }
    }

    /** 인증코드 확인 */
    public ResultData<?> confirmCode(String rawPhone, String inputCode) {
        final String phone = normalize(rawPhone);
        final String code  = (inputCode == null) ? "" : inputCode.trim();

        if (phone.isBlank()) return ResultData.from("F-PHONE", "전화번호를 입력하세요.");
        if (code.isBlank())  return ResultData.from("F-CODE", "코드를 입력해 주세요.");

        String savedHash = redis.opsForValue().get(keyCode(phone));
        if (savedHash == null) {
            return ResultData.from("F-NOTFOUND", "유효한 인증번호가 없습니다. 재전송해 주세요.");
        }

        String calcHash = sha256(code + "|" + phone);
        if (!savedHash.equals(calcHash)) {
            return ResultData.from("F-MISMATCH", "인증번호가 일치하지 않습니다.");
        }

        // 성공: 일회용 소멸
        redis.delete(keyCode(phone));
        return ResultData.from("S-OK", "전화번호 인증이 완료되었습니다.", "phone", phone);
    }
}
