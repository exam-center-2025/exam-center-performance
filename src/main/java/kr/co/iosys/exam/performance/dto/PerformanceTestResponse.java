package kr.co.iosys.exam.performance.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AIDEV-NOTE: 성능 테스트 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceTestResponse {

    private String testId;
    private String testName;
    private TestStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long planId;
    private Integer maxUsers;
    private String scenario;
    private Map<String, Object> results;
    private String errorMessage;
    private Integer testDurationSeconds;
    private Integer progress;

    public enum TestStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}