package kr.co.iosys.exam.performance.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 성능 테스트 마스터 엔티티
 * performance_tests 테이블과 매핑
 */
@Entity
@Table(name = "performance_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceTest {
    
    @Id
    @Column(name = "test_id")
    private String testId;
    
    @Column(name = "test_name", nullable = false)
    private String testName;
    
    @Column(name = "plan_id", nullable = false)
    private Long planId;
    
    @Column(name = "run_type")
    private String runType;
    
    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;
    
    @Column(name = "ramp_up_seconds", nullable = false)
    private Integer rampUpSeconds;
    
    @Column(name = "test_duration_seconds", nullable = false)
    private Integer testDurationSeconds;
    
    @Column(name = "scenario")
    private String scenario;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "result_path")
    private String resultPath;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}