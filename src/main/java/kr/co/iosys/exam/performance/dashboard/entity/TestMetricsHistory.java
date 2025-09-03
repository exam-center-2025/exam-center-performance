package kr.co.iosys.exam.performance.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_metrics_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestMetricsHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "test_id", nullable = false, length = 100)
    private String testId;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "active_users")
    private Integer activeUsers;
    
    @Column(name = "tps", precision = 10, scale = 2)
    private BigDecimal tps;
    
    @Column(name = "avg_response_time", precision = 10, scale = 2)
    private BigDecimal avgResponseTime;
    
    @Column(name = "min_response_time", precision = 10, scale = 2)
    private BigDecimal minResponseTime;
    
    @Column(name = "max_response_time", precision = 10, scale = 2)
    private BigDecimal maxResponseTime;
    
    @Column(name = "p95_response_time", precision = 10, scale = 2)
    private BigDecimal p95ResponseTime;
    
    @Column(name = "p99_response_time", precision = 10, scale = 2)
    private BigDecimal p99ResponseTime;
    
    @Column(name = "success_count")
    private Long successCount;
    
    @Column(name = "error_count")
    private Long errorCount;
    
    @Column(name = "error_rate", precision = 5, scale = 2)
    private BigDecimal errorRate;
}