package kr.co.iosys.exam.performance.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 테스트 결과 요약 엔티티
 * test_results_summary 테이블과 매핑
 */
@Entity
@Table(name = "test_results_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResultsSummary {
    
    @Id
    @Column(name = "test_id")
    private String testId;
    
    @Column(name = "total_requests")
    private Long totalRequests;
    
    @Column(name = "successful_requests")
    private Long successfulRequests;
    
    @Column(name = "failed_requests")
    private Long failedRequests;
    
    @Column(name = "success_rate", precision = 5, scale = 2)
    private BigDecimal successRate;
    
    @Column(name = "avg_response_time", precision = 10, scale = 2)
    private BigDecimal avgResponseTime;
    
    @Column(name = "min_response_time", precision = 10, scale = 2)
    private BigDecimal minResponseTime;
    
    @Column(name = "max_response_time", precision = 10, scale = 2)
    private BigDecimal maxResponseTime;
    
    @Column(name = "p50_response_time", precision = 10, scale = 2)
    private BigDecimal p50ResponseTime;
    
    @Column(name = "p75_response_time", precision = 10, scale = 2)
    private BigDecimal p75ResponseTime;
    
    @Column(name = "p95_response_time", precision = 10, scale = 2)
    private BigDecimal p95ResponseTime;
    
    @Column(name = "p99_response_time", precision = 10, scale = 2)
    private BigDecimal p99ResponseTime;
    
    @Column(name = "max_tps", precision = 10, scale = 2)
    private BigDecimal maxTps;
    
    @Column(name = "avg_tps", precision = 10, scale = 2)
    private BigDecimal avgTps;
    
    @Column(name = "total_data_kb")
    private Long totalDataKb;
    
    @Column(name = "test_duration_seconds")
    private Integer testDurationSeconds;
    
    @Column(name = "max_concurrent_users")
    private Integer maxConcurrentUsers;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}