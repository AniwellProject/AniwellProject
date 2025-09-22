package com.example.RSW.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConfigurationProperties(prefix = "custom.redis")
@Data
public class RedisConfig {

    private boolean enabled;
    private boolean usePersonal;
    private String personalHost;
    private int personalPort;
    private String sharedHost;
    private int sharedPort;
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        if (!enabled) {
            System.err.println("❌ Redis 비활성화됨 (custom.redis.enabled = false)");
            return null;
        }

        String host = usePersonal ? personalHost : sharedHost;
        int port = usePersonal ? personalPort : sharedPort;

        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
            if (password != null && !password.isEmpty()) {
                config.setPassword(password);
            }
            return new LettuceConnectionFactory(config);
        } catch (Exception e) {
            System.err.println("❌ Redis 연결 실패 → 비활성화 상태로 실행됨: " + e.getMessage());
            return null;
        }
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        if (factory == null) {
            System.err.println("❌ RedisTemplate 생성 생략 (Redis 비활성화 상태)");
            return template;
        }

        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

}
