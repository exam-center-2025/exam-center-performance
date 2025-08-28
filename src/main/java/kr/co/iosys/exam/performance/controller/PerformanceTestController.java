package kr.co.iosys.exam.performance.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.iosys.exam.performance.dto.PerformanceTestRequest;
import kr.co.iosys.exam.performance.dto.PerformanceTestResponse;
import kr.co.iosys.exam.performance.model.ExamGroup;
import kr.co.iosys.exam.performance.model.ExamPlan;
import kr.co.iosys.exam.performance.service.DatabaseService;
import kr.co.iosys.exam.performance.service.GatlingRunnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * AIDEV-NOTE: 성능 테스트 관련 REST API 컨트롤러
 * Gatling 성능 테스트의 프로그래매틱 실행 및 관리 담당
 */
@Slf4j
@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Validated
@Tag(name = "Performance Test", description = "성능 테스트 관리 API")
public class PerformanceTestController {

    private final GatlingRunnerService gatlingRunnerService;
    private final DatabaseService databaseService;

    @Operation(summary = "성능 테스트 시작", description = "새로운 성능 테스트를 시작합니다")
    @ApiResponse(responseCode = "200", description = "테스트 시작 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    @PostMapping("/tests/start")
    public PerformanceTestResponse startTest(@Valid @RequestBody PerformanceTestRequest request) {
        log.info("성능 테스트 시작 요청: {}", request.getTestName());
        
        try {
            PerformanceTestResponse response = gatlingRunnerService.startPerformanceTest(request);
            log.info("성능 테스트 시작됨: {}", response.getTestId());
            return response;
        } catch (Exception e) {
            log.error("성능 테스트 시작 실패: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "테스트 상태 조회", description = "특정 테스트의 현재 상태를 조회합니다")
    @ApiResponse(responseCode = "200", description = "상태 조회 성공")
    @ApiResponse(responseCode = "404", description = "테스트를 찾을 수 없음")
    @GetMapping("/tests/{testId}/status")
    public PerformanceTestResponse getTestStatus(
            @Parameter(description = "테스트 ID", required = true)
            @PathVariable @NotBlank String testId) {
        
        log.debug("테스트 상태 조회 요청: {}", testId);
        
        PerformanceTestResponse response = gatlingRunnerService.getTestStatus(testId);
        log.debug("테스트 상태 조회 완료: {} - {}", testId, response.getStatus());
        return response;
    }

    @Operation(summary = "테스트 결과 조회", description = "완료된 테스트의 결과를 조회합니다")
    @ApiResponse(responseCode = "200", description = "결과 조회 성공")
    @ApiResponse(responseCode = "404", description = "테스트를 찾을 수 없음")
    @GetMapping("/tests/{testId}/results")
    public PerformanceTestResponse getTestResults(
            @Parameter(description = "테스트 ID", required = true)
            @PathVariable @NotBlank String testId) {
        
        log.info("테스트 결과 조회 요청: {}", testId);
        
        PerformanceTestResponse response = gatlingRunnerService.getTestStatus(testId);
        if (response.getStatus() != PerformanceTestResponse.TestStatus.COMPLETED) {
            throw new RuntimeException("테스트가 완료되지 않았습니다");
        }
        log.info("테스트 결과 조회 완료: {}", testId);
        return response;
    }

    @Operation(summary = "테스트 중단", description = "실행 중인 테스트를 중단합니다")
    @ApiResponse(responseCode = "200", description = "테스트 중단 성공")
    @ApiResponse(responseCode = "404", description = "테스트를 찾을 수 없음")
    @DeleteMapping("/tests/{testId}")
    public PerformanceTestResponse cancelTest(
            @Parameter(description = "테스트 ID", required = true)
            @PathVariable @NotBlank String testId) {
        
        log.info("테스트 중단 요청: {}", testId);
        
        try {
            PerformanceTestResponse response = gatlingRunnerService.cancelTest(testId);
            log.info("테스트 중단 완료: {}", testId);
            return response;
        } catch (Exception e) {
            log.error("테스트 중단 실패: {} - {}", testId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "모든 테스트 조회", description = "현재 실행 중인 모든 테스트를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/tests")
    public Map<String, PerformanceTestResponse> getAllTests() {
        log.debug("전체 테스트 목록 조회 요청");
        
        Map<String, PerformanceTestResponse> tests = gatlingRunnerService.getAllTests();
        log.debug("전체 테스트 목록 조회 완료: {} 개", tests.size());
        return tests;
    }

    @Operation(summary = "시험 계획 목록 조회", description = "사용 가능한 시험 계획 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "계획 목록 조회 성공")
    @GetMapping("/plans")
    public java.util.List<ExamPlan> getExamPlans() {
        log.debug("시험 계획 목록 조회 요청");
        
        try {
            java.util.List<ExamPlan> plans = databaseService.getExamPlans();
            log.debug("시험 계획 목록 조회 완료");
            return plans;
        } catch (Exception e) {
            log.error("시험 계획 목록 조회 실패: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "그룹 목록 조회", description = "특정 시험 계획의 그룹 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "그룹 목록 조회 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 계획 ID")
    @GetMapping("/plans/{planId}/groups")
    public java.util.List<ExamGroup> getExamGroups(
            @Parameter(description = "시험 계획 ID", required = true)
            @PathVariable Long planId) {
        
        log.debug("그룹 목록 조회 요청: plan_id = {}", planId);
        
        try {
            java.util.List<ExamGroup> groups = databaseService.getExamGroups(planId);
            log.debug("그룹 목록 조회 완료: plan_id = {}", planId);
            return groups;
        } catch (Exception e) {
            log.error("그룹 목록 조회 실패: plan_id = {}, error = {}", planId, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "데이터베이스 연결 상태 확인", description = "데이터베이스 연결 상태를 확인합니다")
    @ApiResponse(responseCode = "200", description = "연결 상태 확인 완료")
    @GetMapping("/health/database")
    public Map<String, Object> checkDatabaseHealth() {
        log.debug("데이터베이스 연결 상태 확인 요청");
        
        boolean connected = databaseService.checkConnection();
        Map<String, Object> result = Map.of(
                "status", connected ? "UP" : "DOWN",
                "database", "postgresql",
                "timestamp", System.currentTimeMillis()
        );
        log.debug("데이터베이스 연결 상태: {}", result.get("status"));
        return result;
    }

    /**
     * AIDEV-NOTE: 전역 예외 처리
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        log.error("API 호출 중 오류 발생", e);
        
        return Map.of(
                "error", "INTERNAL_SERVER_ERROR",
                "message", e.getMessage(),
                "timestamp", System.currentTimeMillis()
        );
    }
}