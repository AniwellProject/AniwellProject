package com.example.RSW.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket 연결 주소 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 웹소켓 연결 주소: /ws
                .setAllowedOriginPatterns("*") // 모든 도메인 허용 (필요시 제한)
                .withSockJS(); // SockJS fallback 지원
    }

    // 메시지 라우팅 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 구독 경로 접두사
        config.setApplicationDestinationPrefixes("/app"); // 메시지 보낼 때 prefix
    }
}
