package kr.co.iosys.exam.performance.dashboard.controller;

import kr.co.iosys.exam.performance.dashboard.dto.TestMetrics;
import kr.co.iosys.exam.performance.dashboard.service.DashboardService;
import kr.co.iosys.exam.performance.dashboard.service.MetricsBroadcastService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

/**
 * WebSocket 메시지 처리 컨트롤러
 * 클라이언트의 WebSocket 메시지를 처리하고 실시간 데이터를 제공
 * 
 * AIDEV-NOTE: STOMP 메시징 프로토콜 사용, 구독/발행 패턴
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardWebSocketController {
    
    private final DashboardService dashboardService;
    private final MetricsBroadcastService metricsBroadcastService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Redis 키 패턴
    private static final String REDIS_KEY_TEST_STATUS = "test:status:%s";
    
    /**
     * 테스트 메트릭 구독 요청 처리
     * 클라이언트가 /app/test/{testId}/subscribe 로 메시지를 보내면 호출
     */
    @MessageMapping("/test/{testId}/subscribe")
    @SendTo("/topic/metrics/{testId}")
    public TestMetrics subscribeToMetrics(@DestinationVariable String testId, 
                                        @Payload Map<String, Object> message) {
        try {
            log.info("테스트 메트릭 구독 요청: testId={}", testId);
            
            // 현재 메트릭 즉시 응답
            Optional<TestMetrics> currentMetrics = dashboardService.getCurrentMetrics(testId);
            
            if (currentMetrics.isPresent()) {
                log.debug("현재 메트릭 전송: testId={}", testId);
                return currentMetrics.get();
            } else {
                // 기본 메트릭 반환 (데이터 없음 상태)
                return TestMetrics.createWithCurrentTime(testId);
            }
            
        } catch (Exception e) {
            log.error("테스트 메트릭 구독 처리 실패: {}", testId, e);
            return TestMetrics.createWithCurrentTime(testId);
        }
    }
    
    /**
     * 테스트 상태 구독 요청 처리
     * 클라이언트가 /app/test/{testId}/status 로 메시지를 보내면 호출
     */
    @MessageMapping("/test/{testId}/status")
    @SendTo("/topic/status/{testId}")
    public StatusMessage subscribeToStatus(@DestinationVariable String testId,
                                         @Payload Map<String, Object> message) {
        try {
            log.info("테스트 상태 구독 요청: testId={}", testId);
            
            // Redis에서 현재 상태 조회
            String statusKey = String.format(REDIS_KEY_TEST_STATUS, testId);
            Object statusData = redisTemplate.opsForValue().get(statusKey);
            
            if (statusData != null) {
                try {
                    // JSON 파싱하여 상태 정보 추출
                    Map<String, Object> statusMap = objectMapper.readValue(statusData.toString(), Map.class);
                    String status = (String) statusMap.getOrDefault("status", "UNKNOWN");
                    String statusMessage = (String) statusMap.getOrDefault("message", "상태 정보 조회됨");
                    
                    log.debug("Redis에서 테스트 상태 조회 완료: testId={}, status={}", testId, status);
                    return new StatusMessage(testId, status, statusMessage, System.currentTimeMillis());
                    
                } catch (Exception parseEx) {
                    log.error("Redis 상태 데이터 파싱 실패: {}", testId, parseEx);
                    return new StatusMessage(testId, "PARSE_ERROR", "상태 데이터 파싱 오류", System.currentTimeMillis());
                }
            } else {
                log.info("Redis에 테스트 상태 정보 없음: testId={}", testId);
                return new StatusMessage(testId, "NOT_FOUND", "테스트 상태 정보를 찾을 수 없습니다.", System.currentTimeMillis());
            }
            
        } catch (Exception e) {
            log.error("테스트 상태 구독 처리 실패: {}", testId, e);
            return new StatusMessage(testId, "ERROR", "상태 조회 실패: " + e.getMessage(), System.currentTimeMillis());
        }
    }
    
    /**
     * 테스트 로그 구독 요청 처리
     * 클라이언트가 /app/test/{testId}/logs 로 메시지를 보내면 호출
     */
    @MessageMapping("/test/{testId}/logs")
    @SendTo("/topic/logs/{testId}")
    public LogMessage subscribeToLogs(@DestinationVariable String testId,
                                    @Payload Map<String, Object> message) {
        try {
            log.info("테스트 로그 구독 요청: testId={}", testId);
            
            // 구독 시작 메시지 반환
            return new LogMessage(testId, "INFO", "로그 구독이 시작되었습니다.", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("테스트 로그 구독 처리 실패: {}", testId, e);
            return new LogMessage(testId, "ERROR", "로그 구독 실패", System.currentTimeMillis());
        }
    }
    
    /**
     * 개별 사용자에게 메시지 전송
     * 관리자 권한이 필요한 기능에 사용
     */
    @MessageMapping("/admin/broadcast")
    @SendToUser("/queue/notifications")
    public NotificationMessage handleAdminBroadcast(@Payload Map<String, Object> message) {
        try {
            String messageText = message.get("message").toString();
            log.info("관리자 브로드캐스트: {}", messageText);
            
            return new NotificationMessage("ADMIN", messageText, System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("관리자 브로드캐스트 처리 실패", e);
            return new NotificationMessage("ERROR", "브로드캐스트 실패", System.currentTimeMillis());
        }
    }
    
    /**
     * WebSocket 연결 상태 확인
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public PongMessage handlePing(@Payload Map<String, Object> message) {
        try {
            String clientId = message.getOrDefault("clientId", "unknown").toString();
            log.debug("Ping 요청 수신: clientId={}", clientId);
            
            return new PongMessage(clientId, "pong", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Ping 처리 실패", e);
            return new PongMessage("error", "ping 처리 실패", System.currentTimeMillis());
        }
    }
    
    // === Message DTOs ===
    
    /**
     * 상태 메시지
     */
    public static class StatusMessage {
        private String testId;
        private String status;
        private String message;
        private Long timestamp;
        
        public StatusMessage(String testId, String status, String message, Long timestamp) {
            this.testId = testId;
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public String getTestId() { return testId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
    
    /**
     * 로그 메시지
     */
    public static class LogMessage {
        private String testId;
        private String level;
        private String message;
        private Long timestamp;
        
        public LogMessage(String testId, String level, String message, Long timestamp) {
            this.testId = testId;
            this.level = level;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public String getTestId() { return testId; }
        public String getLevel() { return level; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
    
    /**
     * 알림 메시지
     */
    public static class NotificationMessage {
        private String type;
        private String message;
        private Long timestamp;
        
        public NotificationMessage(String type, String message, Long timestamp) {
            this.type = type;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public String getType() { return type; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
    
    /**
     * Pong 메시지 (Ping 응답)
     */
    public static class PongMessage {
        private String clientId;
        private String message;
        private Long timestamp;
        
        public PongMessage(String clientId, String message, Long timestamp) {
            this.clientId = clientId;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public String getClientId() { return clientId; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
}