package kr.co.iosys.exam.performance.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 테스트 결과 DTO
 * 완료된 테스트의 최종 결과 및 요약 정보
 * 
 * AIDEV-NOTE: PostgreSQL test_results_summary 테이블과 매핑되는 결과 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {
    
    /**
     * 테스트 ID
     */
    @JsonProperty("testId")
    private String testId;
    
    /**
     * 테스트 이름
     */
    @JsonProperty("testName")
    private String testName;
    
    /**
     * 시험 계획 ID
     */
    @JsonProperty("planId")
    private Long planId;
    
    /**
     * 테스트 상태
     */
    @JsonProperty("status")
    private String status;
    
    /**
     * 테스트 시작 시간
     */
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    /**
     * 테스트 종료 시간
     */
    @JsonProperty("endTime")
    private LocalDateTime endTime;
    
    /**
     * 총 요청 수
     */
    @JsonProperty("totalRequests")
    private Long totalRequests;
    
    /**
     * 성공한 요청 수
     */
    @JsonProperty("successfulRequests")
    private Long successfulRequests;
    
    /**
     * 실패한 요청 수
     */
    @JsonProperty("failedRequests")
    private Long failedRequests;
    
    /**
     * 성공률 (%)
     */
    @JsonProperty("successRate")
    private Double successRate;
    
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
     * P50 응답시간 (밀리초)
     */
    @JsonProperty("p50ResponseTime")
    private Double p50ResponseTime;
    
    /**
     * P75 응답시간 (밀리초)
     */
    @JsonProperty("p75ResponseTime")
    private Double p75ResponseTime;
    
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
     * 최대 TPS
     */
    @JsonProperty("maxTps")
    private Double maxTps;
    
    /**
     * 평균 TPS
     */
    @JsonProperty("avgTps")
    private Double avgTps;
    
    /**
     * 테스트 지속시간 (초)
     */
    @JsonProperty("testDurationSeconds")
    private Integer testDurationSeconds;
    
    /**
     * 최대 동시 사용자 수
     */
    @JsonProperty("maxConcurrentUsers")
    private Integer maxConcurrentUsers;
    
    /**
     * Gatling 리포트 경로
     */
    @JsonProperty("reportPath")
    private String reportPath;
    
    /**
     * 에러 메시지 (테스트 실패 시)
     */
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    /**
     * 실제 테스트 지속시간 계산 (종료시간 - 시작시간, 초 단위)
     */
    public Long getActualDurationSeconds() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).getSeconds();
        }
        return null;
    }
    
    /**
     * 테스트가 성공적으로 완료되었는지 확인
     */
    public boolean isSuccessful() {
        return "COMPLETED".equals(status) && errorMessage == null;
    }
    
    /**
     * 리포트 URL 생성
     */
    public String getReportUrl() {
        if (reportPath != null) {
            return "/dashboard/results/" + testId + "/report";
        }
        return null;
    }
}