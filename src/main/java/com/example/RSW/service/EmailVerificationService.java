// EmailVerificationService.java
package com.example.RSW.service;

import com.example.RSW.vo.ResultData;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final @Qualifier("redisTemplate") RedisTemplate<String, String> redis;
    private final MailService mailService;

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);

    private String keyTx(String txId) { return "email:verify:tx:" + txId; }
    private String keyCooldown(String email) { return "email:verify:cooldown:" + normalize(email); }
    private String normalize(String email) { return email == null ? "" : email.trim().toLowerCase(Locale.ROOT); }

    public ResultData<?> sendCode(String rawEmail, String purpose) {
        final String email = normalize(rawEmail);
        if (email.isBlank()) return ResultData.from("F-EMAIL", "이메일이 비어 있습니다.");

        // 쿨다운 체크
        String cdKey = keyCooldown(email);
        if (Boolean.TRUE.equals(redis.hasKey(cdKey))) {
            Long ttl = redis.getExpire(cdKey, TimeUnit.SECONDS);
            return ResultData.from("F-COOLDOWN", "잠시 후 다시 시도해 주세요.",
                    "retryAfterSec", Math.max(1, ttl == null ? 0 : ttl));
        }

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        String txId = UUID.randomUUID().toString().replace("-", "");

        String p = (purpose == null ? "join" : purpose);
        String hash = sha256(code + "|" + email + "|" + p);
        String payload = email + "|" + p + "|" + hash;

        redis.opsForValue().set(keyTx(txId), payload, CODE_TTL.getSeconds(), TimeUnit.SECONDS);
        redis.opsForValue().set(cdKey, "1", COOLDOWN.getSeconds(), TimeUnit.SECONDS);

        String subject = "[aniwell] 이메일 인증 코드: " + code + " (5분 유효)";
        String body = """
                안녕하세요, aniwell 입니다.

                아래 6자리 인증코드를 가입 화면에 입력해주세요.
                인증코드: %s

                • 유효시간: 5분
                • 요청이 본인이 아니라면 본 메일을 무시해 주세요.

                감사합니다.
                """.formatted(code);

        ResultData sendRd = mailService.send(email, subject, body);
        if (sendRd == null || sendRd.isFail()) {
            redis.delete(keyTx(txId));
            return ResultData.from(sendRd == null ? "F-SEND" : sendRd.getResultCode(),
                    sendRd == null ? "메일 전송 실패" : sendRd.getMsg());
        }

        return ResultData.from("S-OK", "인증코드를 전송했습니다.",
                "txId", txId,
                "ttlSec", CODE_TTL.getSeconds());
    }

    @SuppressWarnings("unchecked")
    public ResultData<?> verifyCode(String txId, String code, String purpose, HttpSession session) {
        if (txId == null || txId.isBlank()) return ResultData.from("F-TX", "txId가 없습니다.");
        if (code == null || code.isBlank()) return ResultData.from("F-CODE", "코드를 입력해 주세요.");

        String payload = redis.opsForValue().get(keyTx(txId));
        if (payload == null) return ResultData.from("F-EXPIRED", "코드가 만료되었거나 잘못되었습니다.");

        String[] parts = payload.split("\\|", 3);
        if (parts.length < 3) return ResultData.from("F-PAYLOAD", "저장된 코드 정보가 손상되었습니다.");

        String email = parts[0];
        String savedPurpose = parts[1];
        String savedHash = parts[2];

        String p = (purpose == null ? "join" : purpose);
        String calcHash = sha256(code + "|" + email + "|" + p);
        if (!savedPurpose.equals(p) || !savedHash.equals(calcHash)) {
            return ResultData.from("F-NOPE", "코드가 일치하지 않습니다.");
        }

        redis.delete(keyTx(txId));

        Set<String> verified = (Set<String>) session.getAttribute("VERIFIED_EMAILS");
        if (verified == null) verified = new HashSet<>();
        verified.add(email);
        session.setAttribute("VERIFIED_EMAILS", verified);

        return ResultData.from("S-OK", "이메일 인증이 완료되었습니다.", "email", email);
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
