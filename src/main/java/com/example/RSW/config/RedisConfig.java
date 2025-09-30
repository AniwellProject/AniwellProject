package com.example.RSW.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    @ConditionalOnProperty(name = "custom.redis.enabled", havingValue = "true")
    public RedisConnectionFactory redisConnectionFactory() {
        String host = usePersonal ? personalHost : sharedHost;
        int port = usePersonal ? personalPort : sharedPort;

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }

        return new LettuceConnectionFactory(config);
    }


    @Bean
    @Primary
    @ConditionalOnProperty(name = "custom.redis.enabled", havingValue = "true")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
