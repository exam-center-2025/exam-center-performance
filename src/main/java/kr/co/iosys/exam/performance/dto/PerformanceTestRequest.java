package kr.co.iosys.exam.performance.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * AIDEV-NOTE: 성능 테스트 시작 요청 DTO
 */
@Data
public class PerformanceTestRequest {

    @NotBlank
    private String testName;

    @NotNull
    @Min(1)
    private Long planId;

    @NotNull
    @Min(1)
    private Integer maxUsers;

    @Min(1)
    private Integer rampUpDurationSeconds = 60;

    @Min(1)
    private Integer testDurationSeconds = 300;

    private String runType = "TEST";

    private String scenario = "NORMAL_USER";

    private Map<String, Object> additionalConfig;
}