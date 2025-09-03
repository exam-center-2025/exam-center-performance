package kr.co.iosys.exam.performance.dashboard.service;

import kr.co.iosys.exam.performance.dashboard.dto.TestMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

/**
 * 실시간 메트릭 브로드캐스트 서비스
 * Redis에서 실시간 메트릭을 조회하여 WebSocket으로 클라이언트에 전송
 * 
 * AIDEV-NOTE: 1초마다 실행되는 스케줄러, 성능 최적화를 위해 활성 테스트만 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsBroadcastService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Redis 키 패턴
    private static final String REDIS_KEY_ACTIVE_TESTS = "tests:active";
    private static final String REDIS_KEY_CURRENT_METRICS = "metrics:current:%s";
    
    // WebSocket 토픽 패턴
    private static final String WEBSOCKET_TOPIC_METRICS = "/topic/metrics/%s";
    private static final String WEBSOCKET_TOPIC_STATUS = "/topic/status/%s";
    
    /**
     * 실시간 메트릭 브로드캐스트
     * 1초마다 실행되어 활성 테스트의 메트릭을 WebSocket으로 전송
     */
    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    public void broadcastMetrics() {
        try {
            Set<Object> activeTests = redisTemplate.opsForSet().members(REDIS_KEY_ACTIVE_TESTS);
            
            if (activeTests == null || activeTests.isEmpty()) {
                return; // 활성 테스트가 없으면 종료
            }
            
            for (Object testIdObj : activeTests) {
                String testId = testIdObj.toString();
                broadcastTestMetrics(testId);
            }
            
        } catch (Exception e) {
            log.error("메트릭 브로드캐스트 중 오류 발생", e);
        }
    }
    
    /**
     * 특정 테스트의 메트릭 브로드캐스트
     */
    private void broadcastTestMetrics(String testId) {
        try {
            // Redis에서 현재 메트릭 조회
            String metricsKey = String.format(REDIS_KEY_CURRENT_METRICS, testId);
            Object metricsData = redisTemplate.opsForValue().get(metricsKey);
            
            if (metricsData != null) {
                // JSON을 TestMetrics 객체로 변환
                TestMetrics metrics = objectMapper.readValue(metricsData.toString(), TestMetrics.class);
                
                // WebSocket 토픽으로 전송
                String topic = String.format(WEBSOCKET_TOPIC_METRICS, testId);
                messagingTemplate.convertAndSend(topic, metrics);
                
                // 로그는 디버그 레벨로만 출력 (성능 고려)
                if (log.isDebugEnabled()) {
                    log.debug("메트릭 브로드캐스트 완료: testId={}, activeUsers={}, tps={}", 
                            testId, metrics.getActiveUsers(), metrics.getTps());
                }
            }
            
        } catch (Exception e) {
            log.error("테스트 메트릭 브로드캐스트 실패: {}", testId, e);
        }
    }
    
    /**
     * 특정 테스트의 상태 업데이트 브로드캐스트
     */
    public void broadcastStatusUpdate(String testId, String status, String message) {
        try {
            StatusUpdate statusUpdate = new StatusUpdate(testId, status, message, System.currentTimeMillis());
            
            String topic = String.format(WEBSOCKET_TOPIC_STATUS, testId);
            messagingTemplate.convertAndSend(topic, statusUpdate);
            
            log.info("상태 업데이트 브로드캐스트: testId={}, status={}", testId, status);
            
        } catch (Exception e) {
            log.error("상태 업데이트 브로드캐스트 실패: {}", testId, e);
        }
    }
    
    /**
     * 테스트 로그 브로드캐스트
     */
    public void broadcastLogMessage(String testId, String level, String message) {
        try {
            LogMessage logMessage = new LogMessage(testId, level, message, System.currentTimeMillis());
            
            String topic = String.format("/topic/logs/%s", testId);
            messagingTemplate.convertAndSend(topic, logMessage);
            
            if (log.isDebugEnabled()) {
                log.debug("로그 메시지 브로드캐스트: testId={}, level={}", testId, level);
            }
            
        } catch (Exception e) {
            log.error("로그 메시지 브로드캐스트 실패: {}", testId, e);
        }
    }
    
    /**
     * 직접 메트릭 브로드캐스트 (외부 호출용)
     */
    public void broadcastMetrics(TestMetrics metrics) {
        try {
            String topic = String.format(WEBSOCKET_TOPIC_METRICS, metrics.getTestId());
            messagingTemplate.convertAndSend(topic, metrics);
            
            log.debug("직접 메트릭 브로드캐스트: {}", metrics.getTestId());
            
        } catch (Exception e) {
            log.error("직접 메트릭 브로드캐스트 실패: {}", metrics.getTestId(), e);
        }
    }
    
    /**
     * 상태 업데이트 DTO
     */
    public static class StatusUpdate {
        private String testId;
        private String status;
        private String message;
        private Long timestamp;
        
        public StatusUpdate(String testId, String status, String message, Long timestamp) {
            this.testId = testId;
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getTestId() { return testId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
    
    /**
     * 로그 메시지 DTO
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
        
        // Getters
        public String getTestId() { return testId; }
        public String getLevel() { return level; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
}