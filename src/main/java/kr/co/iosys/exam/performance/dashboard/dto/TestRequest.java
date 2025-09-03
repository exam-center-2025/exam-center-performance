package kr.co.iosys.exam.performance.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * 성능 테스트 요청 DTO
 * 테스트 파라미터 설정 및 실행 요청에 사용
 * 
 * AIDEV-NOTE: 대시보드에서 테스트 실행 시 사용되는 핵심 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRequest {
    
    /**
     * 시험 계획 ID
     */
    @NotNull(message = "시험 계획 ID는 필수입니다")
    private Long planId;
    
    /**
     * 실행 타입 (TEST, PROD 등)
     */
    @NotBlank(message = "실행 타입은 필수입니다")
    private String runType;
    
    /**
     * 최대 사용자 수
     */
    @NotNull(message = "최대 사용자 수는 필수입니다")
    @Min(value = 1, message = "최대 사용자 수는 1 이상이어야 합니다")
    private Integer maxUsers;
    
    /**
     * 램프업 시간 (초)
     */
    @NotNull(message = "램프업 시간은 필수입니다")
    @Min(value = 0, message = "램프업 시간은 0 이상이어야 합니다")
    private Integer rampUpSeconds;
    
    /**
     * 테스트 지속 시간 (초)
     */
    @NotNull(message = "테스트 지속 시간은 필수입니다")
    @Min(value = 1, message = "테스트 지속 시간은 1 이상이어야 합니다")
    private Integer testDurationSeconds;
    
    /**
     * 테스트 시나리오 (BASIC, COMPLETE, STRESS)
     */
    @NotBlank(message = "테스트 시나리오는 필수입니다")
    private String scenario;
    
    /**
     * 테스트 이름 (선택사항)
     */
    private String testName;
    
    /**
     * 추가 설정 (JSON 형태의 문자열)
     */
    private String additionalConfig;
}