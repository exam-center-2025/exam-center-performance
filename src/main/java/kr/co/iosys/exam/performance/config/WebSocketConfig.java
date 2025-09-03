package kr.co.iosys.exam.performance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * 실시간 메트릭 및 로그 전송을 위한 STOMP over WebSocket 설정
 * 
 * AIDEV-NOTE: SockJS fallback 지원, CORS 설정 포함
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트가 WebSocket 연결을 위해 사용하는 엔드포인트
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 개발 환경에서는 모든 origin 허용
                .withSockJS()  // SockJS fallback 지원
                .setSessionCookieNeeded(false);  // 쿠키 불필요
        
        // 개발 환경용 직접 WebSocket 연결도 지원
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    /**
     * 메시지 브로커 설정
     * 클라이언트 간 메시지 라우팅을 위한 브로커 구성
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 destination prefix
        // 예: /topic/metrics/{testId}, /topic/logs/{testId}
        config.enableSimpleBroker("/topic", "/queue");
        
        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 prefix
        // 예: /app/test/{testId}/subscribe
        config.setApplicationDestinationPrefixes("/app");
        
        // 사용자별 개인 메시지를 위한 prefix
        config.setUserDestinationPrefix("/user");
    }
}