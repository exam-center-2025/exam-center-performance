package kr.co.iosys.exam.performance.dashboard.service;

import kr.co.iosys.exam.performance.dashboard.dto.TestMetrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 실시간 모니터링 서비스
 * Redis 기반 실시간 데이터 저장 및 조회
 * 
 * AIDEV-NOTE: 시계열 데이터는 Sorted Set 사용, TTL 설정으로 메모리 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeMonitoringService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Redis 키 패턴
    private static final String REDIS_KEY_CURRENT_METRICS = "metrics:current:%s";
    private static final String REDIS_KEY_TIMELINE = "timeline:%s:%s";
    private static final String REDIS_KEY_LOGS = "logs:%s";
    private static final String REDIS_KEY_TEST_STATUS = "test:status:%s";
    
    // TTL 설정 (시간)
    private static final Duration CURRENT_METRICS_TTL = Duration.ofSeconds(10);
    private static final Duration TIMELINE_TTL = Duration.ofHours(24);
    private static final Duration LOGS_TTL = Duration.ofHours(1);
    private static final Duration STATUS_TTL = Duration.ofHours(1);
    
    /**
     * 실시간 메트릭 저장
     * 현재 메트릭과 시계열 데이터를 모두 저장
     */
    public void saveCurrentMetrics(TestMetrics metrics) {
        try {
            String testId = metrics.getTestId();
            long timestamp = metrics.getTimestamp() != null ? metrics.getTimestamp() : Instant.now().toEpochMilli();
            
            // 1. 현재 메트릭 저장 (JSON)
            String currentKey = String.format(REDIS_KEY_CURRENT_METRICS, testId);
            String metricsJson = objectMapper.writeValueAsString(metrics);
            redisTemplate.opsForValue().set(currentKey, metricsJson, CURRENT_METRICS_TTL);
            
            // 2. 시계열 데이터 저장 (Sorted Set)
            saveTimelineData(testId, timestamp, metrics);
            
            log.debug("실시간 메트릭 저장 완료: testId={}", testId);
            
        } catch (Exception e) {
            log.error("실시간 메트릭 저장 실패: {}", metrics.getTestId(), e);
        }
    }
    
    /**
     * 시계열 데이터 저장
     * TPS, 응답시간, 활성사용자, 에러율을 각각 저장
     */
    private void saveTimelineData(String testId, long timestamp, TestMetrics metrics) {
        try {
            // TPS 저장
            if (metrics.getTps() != null) {
                String tpsKey = String.format(REDIS_KEY_TIMELINE, testId, "tps");
                redisTemplate.opsForZSet().add(tpsKey, metrics.getTps().toString(), timestamp);
                redisTemplate.expire(tpsKey, TIMELINE_TTL);
            }
            
            // 평균 응답시간 저장
            if (metrics.getAvgResponseTime() != null) {
                String responseTimeKey = String.format(REDIS_KEY_TIMELINE, testId, "response_time");
                redisTemplate.opsForZSet().add(responseTimeKey, metrics.getAvgResponseTime().toString(), timestamp);
                redisTemplate.expire(responseTimeKey, TIMELINE_TTL);
            }
            
            // 활성 사용자 저장
            if (metrics.getActiveUsers() != null) {
                String activeUsersKey = String.format(REDIS_KEY_TIMELINE, testId, "active_users");
                redisTemplate.opsForZSet().add(activeUsersKey, metrics.getActiveUsers().toString(), timestamp);
                redisTemplate.expire(activeUsersKey, TIMELINE_TTL);
            }
            
            // 에러율 저장
            if (metrics.getErrorRate() != null) {
                String errorRateKey = String.format(REDIS_KEY_TIMELINE, testId, "error_rate");
                redisTemplate.opsForZSet().add(errorRateKey, metrics.getErrorRate().toString(), timestamp);
                redisTemplate.expire(errorRateKey, TIMELINE_TTL);
            }
            
        } catch (Exception e) {
            log.error("시계열 데이터 저장 실패: {}", testId, e);
        }
    }
    
    /**
     * 현재 메트릭 조회
     */
    public TestMetrics getCurrentMetrics(String testId) {
        try {
            String currentKey = String.format(REDIS_KEY_CURRENT_METRICS, testId);
            Object metricsData = redisTemplate.opsForValue().get(currentKey);
            
            if (metricsData != null) {
                return objectMapper.readValue(metricsData.toString(), TestMetrics.class);
            }
            
        } catch (Exception e) {
            log.error("현재 메트릭 조회 실패: {}", testId, e);
        }
        
        return null;
    }
    
    /**
     * 시계열 데이터 조회 (최근 N분)
     */
    public List<TimeseriesData> getTimelineData(String testId, String metricType, int minutes) {
        List<TimeseriesData> result = new ArrayList<>();
        
        try {
            String timelineKey = String.format(REDIS_KEY_TIMELINE, testId, metricType);
            long endTime = Instant.now().toEpochMilli();
            long startTime = endTime - (minutes * 60 * 1000L);
            
            Set<Object> dataPoints = redisTemplate.opsForZSet().rangeByScore(timelineKey, startTime, endTime);
            
            if (dataPoints != null) {
                for (Object dataPoint : dataPoints) {
                    try {
                        Double value = Double.parseDouble(dataPoint.toString());
                        Long timestamp = redisTemplate.opsForZSet().score(timelineKey, dataPoint).longValue();
                        result.add(new TimeseriesData(timestamp, value));
                    } catch (NumberFormatException e) {
                        log.warn("시계열 데이터 파싱 실패: {}", dataPoint);
                    }
                }
            }
            
            log.debug("시계열 데이터 조회: testId={}, type={}, count={}", testId, metricType, result.size());
            
        } catch (Exception e) {
            log.error("시계열 데이터 조회 실패: testId={}, type={}", testId, metricType, e);
        }
        
        return result;
    }
    
    /**
     * 테스트 로그 추가
     */
    public void addLogMessage(String testId, String level, String message) {
        try {
            String logsKey = String.format(REDIS_KEY_LOGS, testId);
            
            LogEntry logEntry = new LogEntry(System.currentTimeMillis(), level, message);
            String logJson = objectMapper.writeValueAsString(logEntry);
            
            // 리스트 앞쪽에 추가 (최신이 위에)
            redisTemplate.opsForList().leftPush(logsKey, logJson);
            
            // 최대 1000개로 제한
            redisTemplate.opsForList().trim(logsKey, 0, 999);
            
            // TTL 설정
            redisTemplate.expire(logsKey, LOGS_TTL);
            
            log.debug("테스트 로그 추가: testId={}, level={}", testId, level);
            
        } catch (Exception e) {
            log.error("테스트 로그 추가 실패: {}", testId, e);
        }
    }
    
    /**
     * 테스트 로그 조회 (최근 N개)
     */
    public List<LogEntry> getRecentLogs(String testId, int count) {
        List<LogEntry> logs = new ArrayList<>();
        
        try {
            String logsKey = String.format(REDIS_KEY_LOGS, testId);
            List<Object> logData = redisTemplate.opsForList().range(logsKey, 0, count - 1);
            
            if (logData != null) {
                for (Object logItem : logData) {
                    try {
                        LogEntry logEntry = objectMapper.readValue(logItem.toString(), LogEntry.class);
                        logs.add(logEntry);
                    } catch (Exception e) {
                        log.warn("로그 파싱 실패: {}", logItem);
                    }
                }
            }
            
            log.debug("테스트 로그 조회: testId={}, count={}", testId, logs.size());
            
        } catch (Exception e) {
            log.error("테스트 로그 조회 실패: {}", testId, e);
        }
        
        return logs;
    }
    
    /**
     * 테스트 완료 시 데이터 정리
     */
    public void cleanupTestData(String testId) {
        try {
            // 현재 메트릭 삭제
            String currentKey = String.format(REDIS_KEY_CURRENT_METRICS, testId);
            redisTemplate.delete(currentKey);
            
            // 시계열 데이터 삭제 (또는 TTL 단축)
            String[] metricTypes = {"tps", "response_time", "active_users", "error_rate"};
            for (String metricType : metricTypes) {
                String timelineKey = String.format(REDIS_KEY_TIMELINE, testId, metricType);
                redisTemplate.expire(timelineKey, Duration.ofMinutes(10)); // 10분 후 삭제
            }
            
            // 로그는 1시간 후 자동 삭제 (TTL 유지)
            
            log.info("테스트 데이터 정리 완료: {}", testId);
            
        } catch (Exception e) {
            log.error("테스트 데이터 정리 실패: {}", testId, e);
        }
    }
    
    // DTO Classes
    
    /**
     * 시계열 데이터 포인트
     */
    public static class TimeseriesData {
        private Long timestamp;
        private Double value;
        
        public TimeseriesData(Long timestamp, Double value) {
            this.timestamp = timestamp;
            this.value = value;
        }
        
        public Long getTimestamp() { return timestamp; }
        public Double getValue() { return value; }
    }
    
    /**
     * 로그 엔트리
     */
    public static class LogEntry {
        private Long timestamp;
        private String level;
        private String message;
        
        public LogEntry() {}
        
        public LogEntry(Long timestamp, String level, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
        }
        
        public Long getTimestamp() { return timestamp; }
        public String getLevel() { return level; }
        public String getMessage() { return message; }
        
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
        public void setLevel(String level) { this.level = level; }
        public void setMessage(String message) { this.message = message; }
    }
}