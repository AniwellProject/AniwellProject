package com.example.RSW.service;

import com.example.RSW.vo.Member;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Firebase Custom Token 생성 (Redis 캐싱 포함)
     */
    public String createCustomToken(Member member) {
        String uid = (member.getUid() != null && !member.getUid().isEmpty())
                ? member.getUid()
                : "member-" + member.getId();

        String redisKey = "firebase:token:" + member.getId();
        String cachedToken = redisTemplate.opsForValue().get(redisKey);
        if (cachedToken != null) {
            System.out.println("✅ [Redis] 캐시된 커스텀 토큰 사용");
            return cachedToken;
        }

        Map<String, Object> claims = new HashMap<>();
        if (member.getEmail() != null) {
            claims.put("email", member.getEmail());
        }

        try {
            String token = FirebaseAuth.getInstance().createCustomToken(uid, claims);
            redisTemplate.opsForValue().set(redisKey, token, 6, TimeUnit.HOURS);
            System.out.println("✅ Firebase 커스텀 토큰 발급 및 캐싱 완료");
            return token;
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Firebase 커스텀 토큰 생성 실패: " + e.getMessage());
            throw new RuntimeException("Firebase 토큰 생성 실패", e);
        }
    }
}
