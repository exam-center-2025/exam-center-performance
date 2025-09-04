package kr.co.iosys.exam.performance.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * 실시간 테스트 메트릭 DTO
 * WebSocket을 통한 실시간 모니터링 데이터 전송에 사용
 * 
 * AIDEV-NOTE: Redis에서 조회한 실시간 메트릭을 WebSocket으로 전송하는 핵심 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestMetrics {
    
    /**
     * 테스트 ID
     */
    @JsonProperty("testId")
    private String testId;
    
    /**
     * 메트릭 타임스탬프 (밀리초)
     */
    @JsonProperty("timestamp")
    private Long timestamp;
    
    /**
     * 현재 활성 사용자 수
     */
    @JsonProperty("activeUsers")
    private Integer activeUsers;
    
    /**
     * 초당 트랜잭션 수 (TPS)
     */
    @JsonProperty("tps")
    private Double tps;
    
    /**
     * 평균 응답시간 (밀리초)
     */
    @JsonProperty("avgResponseTime")
    private Double avgResponseTime;
    
    /**
     * 최소 응답시간 (밀리초)
     */
    @JsonProperty("minResponseTime")
    private Double minResponseTime;
    
    /**
     * 최대 응답시간 (밀리초)
     */
    @JsonProperty("maxResponseTime")
    private Double maxResponseTime;
    
    /**
     * P95 응답시간 (밀리초)
     */
    @JsonProperty("p95ResponseTime")
    private Double p95ResponseTime;
    
    /**
     * P99 응답시간 (밀리초)
     */
    @JsonProperty("p99ResponseTime")
    private Double p99ResponseTime;
    
    /**
     * 성공 요청 수
     */
    @JsonProperty("successCount")
    private Long successCount;
    
    /**
     * 에러 요청 수
     */
    @JsonProperty("errorCount")
    private Long errorCount;
    
    /**
     * 에러율 (%)
     */
    @JsonProperty("errorRate")
    private Double errorRate;
    
    /**
     * 테스트 진행률 (%)
     */
    @JsonProperty("progress")
    private Double progress;
    
    /**
     * 총 요청 수 - AIDEV-NOTE: Frontend에서 정확한 에러 수 계산을 위해 추가
     */
    @JsonProperty("totalRequests")
    private Long totalRequests;
    
    /**
     * 현재 시간의 메트릭 생성
     */
    public static TestMetrics createWithCurrentTime(String testId) {
        return TestMetrics.builder()
                .testId(testId)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
    
    /**
     * 전체 요청 수 계산
     * AIDEV-NOTE: totalRequests가 직접 설정된 경우 해당 값 사용, 아니면 계산
     */
    public Long getTotalRequests() {
        if (totalRequests != null) {
            return totalRequests;
        }
        return (successCount != null ? successCount : 0L) + (errorCount != null ? errorCount : 0L);
    }
    
    /**
     * 메트릭 빌드 시 totalRequests 자동 계산 - AIDEV-NOTE: 빌더 패턴에서 자동 계산 지원
     */
    public static class TestMetricsBuilder {
        public TestMetrics build() {
            TestMetrics metrics = new TestMetrics(testId, timestamp, activeUsers, tps, avgResponseTime, 
                    minResponseTime, maxResponseTime, p95ResponseTime, p99ResponseTime, 
                    successCount, errorCount, errorRate, progress, totalRequests);
            
            // totalRequests가 설정되지 않은 경우 자동 계산
            if (metrics.totalRequests == null && (metrics.successCount != null || metrics.errorCount != null)) {
                metrics.totalRequests = (metrics.successCount != null ? metrics.successCount : 0L) 
                        + (metrics.errorCount != null ? metrics.errorCount : 0L);
            }
            
            return metrics;
        }
    }
}