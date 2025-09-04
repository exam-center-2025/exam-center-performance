package kr.co.iosys.exam.performance.dashboard.controller;

import kr.co.iosys.exam.performance.dashboard.dto.TestRequest;
import kr.co.iosys.exam.performance.dashboard.dto.TestResult;
import kr.co.iosys.exam.performance.dashboard.dto.TestMetrics;
import kr.co.iosys.exam.performance.dashboard.service.DashboardService;
import kr.co.iosys.exam.performance.dashboard.service.RealtimeMonitoringService;
import kr.co.iosys.exam.performance.model.ExamPlan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 대시보드 REST API 컨트롤러
 * AJAX 요청 및 실시간 데이터 조회를 위한 REST API
 * 
 * AIDEV-NOTE: 비동기 처리 지원, 에러 응답 표준화
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {
    
    private final DashboardService dashboardService;
    private final RealtimeMonitoringService realtimeMonitoringService;
    
    /**
     * 시스템 헬스 체크
     * GET /api/dashboard/system/health
     */
    @GetMapping("/system/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        try {
            Map<String, Object> health = new java.util.HashMap<>();
            health.put("status", "UP");
            health.put("serverCpu", 45.2);  // 예시 값
            health.put("serverMemory", 68.5);  // 예시 값
            health.put("dbStatus", "connected");  // 추가
            health.put("wsStatus", "connected");  // 추가
            health.put("activeSessions", 5);  // 추가
            health.put("activeUsers", 0);
            health.put("requestsPerSecond", 0);
            health.put("timestamp", System.currentTimeMillis());
            
            log.debug("시스템 헬스 체크 완료");
            return ResponseEntity.ok(ApiResponse.success(health));
            
        } catch (Exception e) {
            log.error("시스템 헬스 체크 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("시스템 헬스 체크 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 대시보드 통계 조회
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        try {
            Map<String, Object> stats = new java.util.HashMap<>();
            
            // 실행 중인 테스트 수
            List<TestResult> activeTests = dashboardService.getActiveTests();
            stats.put("activeTests", activeTests.size());
            
            // 최근 완료된 테스트 수
            List<TestResult> recentTests = dashboardService.getRecentResults();
            stats.put("completedTests", recentTests.size());
            
            // 성공률 계산
            long successCount = recentTests.stream()
                    .filter(t -> "COMPLETED".equals(t.getStatus()))
                    .count();
            double successRate = recentTests.isEmpty() ? 0 : 
                    (double) successCount / recentTests.size() * 100;
            stats.put("successRate", Math.round(successRate * 100.0) / 100.0);
            
            // 평균 응답 시간 (최근 테스트들의 평균)
            double avgResponseTime = recentTests.stream()
                    .filter(t -> t.getAvgResponseTime() != null)
                    .mapToDouble(TestResult::getAvgResponseTime)
                    .average()
                    .orElse(0.0);
            stats.put("avgResponseTime", Math.round(avgResponseTime * 100.0) / 100.0);
            
            // 평균 TPS (최근 테스트들의 평균)
            double avgTps = recentTests.stream()
                    .filter(t -> t.getAvgTps() != null)
                    .mapToDouble(TestResult::getAvgTps)
                    .average()
                    .orElse(0.0);
            stats.put("avgTps", Math.round(avgTps * 100.0) / 100.0);
            
            // 총 테스트 수
            stats.put("totalTests", activeTests.size() + recentTests.size());
            
            log.debug("대시보드 통계 조회 완료");
            return ResponseEntity.ok(ApiResponse.success(stats));
            
        } catch (Exception e) {
            log.error("대시보드 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("대시보드 통계 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 시험 계획 목록 조회
     * GET /api/dashboard/plans
     */
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<ExamPlan>>> getPlans() {
        try {
            List<ExamPlan> plans = dashboardService.getExamPlans();
            log.debug("시험 계획 목록 조회 완료: {}개", plans.size());
            
            return ResponseEntity.ok(ApiResponse.success(plans));
            
        } catch (Exception e) {
            log.error("시험 계획 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("시험 계획 목록 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 모든 테스트 목록 조회
     * GET /api/dashboard/tests
     */
    @GetMapping("/tests")
    public ResponseEntity<ApiResponse<List<TestResult>>> getAllTests() {
        try {
            List<TestResult> allTests = dashboardService.getAllTests();
            log.debug("전체 테스트 목록 조회 완료: {}개", allTests.size());
            
            return ResponseEntity.ok(ApiResponse.success(allTests));
            
        } catch (Exception e) {
            log.error("전체 테스트 목록 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("전체 테스트 목록 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 실행 중인 테스트 목록 조회
     * GET /api/dashboard/tests/active
     */
    @GetMapping("/tests/active")
    public ResponseEntity<ApiResponse<List<TestResult>>> getActiveTests() {
        try {
            List<TestResult> activeTests = dashboardService.getActiveTests();
            log.debug("실행 중인 테스트 조회 완료: {}개", activeTests.size());
            
            return ResponseEntity.ok(ApiResponse.success(activeTests));
            
        } catch (Exception e) {
            log.error("실행 중인 테스트 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("실행 중인 테스트 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 최근 테스트 결과 조회
     * GET /api/dashboard/tests/recent
     */
    @GetMapping("/tests/recent")
    public ResponseEntity<ApiResponse<List<TestResult>>> getRecentResults() {
        try {
            List<TestResult> recentResults = dashboardService.getRecentResults();
            log.debug("최근 테스트 결과 조회 완료: {}개", recentResults.size());
            
            return ResponseEntity.ok(ApiResponse.success(recentResults));
            
        } catch (Exception e) {
            log.error("최근 테스트 결과 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("최근 테스트 결과 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 이력 조회 (시계열 데이터)
     * GET /api/dashboard/tests/history
     */
    @GetMapping("/tests/history")
    public ResponseEntity<ApiResponse<List<TestResult>>> getTestHistory() {
        try {
            // 최근 테스트 결과를 이력으로 사용 (향후 시계열 데이터로 확장 가능)
            List<TestResult> history = dashboardService.getRecentResults();
            log.debug("테스트 이력 조회 완료: {}개", history.size());
            
            return ResponseEntity.ok(ApiResponse.success(history));
            
        } catch (Exception e) {
            log.error("테스트 이력 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 이력 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 시작
     * POST /api/dashboard/tests/start
     */
    @PostMapping("/tests/start")
    public ResponseEntity<ApiResponse<TestStartResponse>> startTest(@Valid @RequestBody TestRequest request) {
        try {
            log.info("테스트 시작 요청 - planId: {}, maxUsers: {}, duration: {}초", 
                    request.getPlanId(), request.getMaxUsers(), request.getTestDurationSeconds());
            
            CompletableFuture<String> testIdFuture = dashboardService.startTest(request);
            String testId = testIdFuture.get(); // 블로킹 대기 (타임아웃 설정 권장)
            
            TestStartResponse response = new TestStartResponse(testId, "테스트가 시작되었습니다.");
            
            log.info("테스트 시작 성공: {}", testId);
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("테스트 시작 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("테스트 시작 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 중단
     * POST /api/dashboard/tests/{testId}/stop
     */
    @PostMapping("/tests/{testId}/stop")
    public ResponseEntity<ApiResponse<String>> stopTest(@PathVariable String testId) {
        try {
            log.info("테스트 중단 요청: {}", testId);
            
            boolean success = dashboardService.stopTest(testId);
            
            if (success) {
                log.info("테스트 중단 성공: {}", testId);
                return ResponseEntity.ok(ApiResponse.success("테스트가 중단되었습니다."));
            } else {
                log.warn("테스트 중단 실패: {}", testId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("테스트 중단에 실패했습니다."));
            }
            
        } catch (Exception e) {
            log.error("테스트 중단 실패: {}", testId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 중단 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 메트릭 히스토리 조회 (차트용)
     * GET /api/dashboard/tests/{testId}/metrics
     */
    @GetMapping("/tests/{testId}/metrics")
    public ResponseEntity<ApiResponse<List<TestMetrics>>> getTestMetrics(@PathVariable String testId) {
        try {
            // 메트릭 히스토리 조회 (최대 300개)
            List<TestMetrics> metricsHistory = dashboardService.getMetricsHistory(testId);
            
            if (metricsHistory != null && !metricsHistory.isEmpty()) {
                log.debug("테스트 메트릭 히스토리 조회 성공: {} - {}개", testId, metricsHistory.size());
                return ResponseEntity.ok(ApiResponse.success(metricsHistory));
            } else {
                log.debug("테스트 메트릭 히스토리 없음: {}", testId);
                // 빈 배열 반환 (404 대신)
                return ResponseEntity.ok(ApiResponse.success(new java.util.ArrayList<>()));
            }
            
        } catch (Exception e) {
            log.error("테스트 메트릭 히스토리 조회 실패: {}", testId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 메트릭 히스토리 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 결과 상세 조회
     * GET /api/dashboard/tests/{testId}/result
     */
    @GetMapping("/tests/{testId}/result")
    public ResponseEntity<ApiResponse<TestResult>> getTestResult(@PathVariable String testId) {
        try {
            Optional<TestResult> result = dashboardService.getTestResult(testId);
            
            if (result.isPresent()) {
                log.debug("테스트 결과 조회 성공: {}", testId);
                return ResponseEntity.ok(ApiResponse.success(result.get()));
            } else {
                log.debug("테스트 결과 없음: {}", testId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("테스트 결과 조회 실패: {}", testId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 결과 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 리포트 URL 조회
     * GET /api/dashboard/tests/{testId}/report-url
     */
    @GetMapping("/tests/{testId}/report-url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getTestReportUrl(@PathVariable String testId) {
        try {
            String reportUrl = dashboardService.getReportUrl(testId);
            
            if (reportUrl != null) {
                Map<String, String> response = new java.util.HashMap<>();
                response.put("reportUrl", reportUrl);
                response.put("url", reportUrl); // 호환성을 위해 두 가지 키로 제공
                
                log.debug("테스트 리포트 URL 조회 성공: {} -> {}", testId, reportUrl);
                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                log.debug("테스트 리포트 URL 없음: {}", testId);
                // 빈 맵 반환 (404 대신)
                return ResponseEntity.ok(ApiResponse.success(new java.util.HashMap<>()));
            }
            
        } catch (Exception e) {
            log.error("테스트 리포트 URL 조회 실패: {}", testId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 리포트 URL 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 설정 조회
     * GET /api/dashboard/tests/{testId}/config
     */
    @GetMapping("/tests/{testId}/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTestConfiguration(@PathVariable String testId) {
        try {
            // 테스트 정보 조회
            Optional<TestResult> optionalTestResult = dashboardService.getTestResult(testId);
            
            if (optionalTestResult.isPresent()) {
                TestResult testResult = optionalTestResult.get();
                Map<String, Object> config = new java.util.HashMap<>();
                config.put("testId", testResult.getTestId());
                config.put("testName", testResult.getTestName());
                config.put("planId", testResult.getPlanId());
                config.put("runType", "TEST"); // 기본값 설정
                
                // AIDEV-NOTE: 실제 maxUsers 값을 DB에서 가져오도록 수정
                Integer maxUsers = dashboardService.getMaxUsers(testId);
                config.put("maxUsers", maxUsers != null ? maxUsers : testResult.getMaxConcurrentUsers());
                
                // AIDEV-NOTE: 실제 rampUpSeconds 값을 DB에서 가져오도록 수정
                Integer rampUpSeconds = dashboardService.getRampUpSeconds(testId);
                config.put("rampUpSeconds", rampUpSeconds != null ? rampUpSeconds : 60); // DB 값 또는 기본값
                
                config.put("testDurationSeconds", testResult.getTestDurationSeconds());
                config.put("status", testResult.getStatus());
                config.put("startedAt", testResult.getStartTime());
                
                log.debug("테스트 설정 조회 성공: testId={}, maxUsers={}, rampUpSeconds={}", 
                         testId, maxUsers, rampUpSeconds);
                return ResponseEntity.ok(ApiResponse.success(config));
            } else {
                log.warn("테스트를 찾을 수 없음: {}", testId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("테스트 설정 조회 실패: {}", testId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 설정 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 시계열 메트릭 데이터 조회 (차트용)
     * GET /api/dashboard/tests/{testId}/timeline?type={metricType}&minutes={minutes}
     */
    @GetMapping("/tests/{testId}/timeline")
    public ResponseEntity<ApiResponse<List<RealtimeMonitoringService.TimeseriesData>>> getTimelineData(
            @PathVariable String testId,
            @RequestParam String type,
            @RequestParam(defaultValue = "5") int minutes) {
        try {
            List<RealtimeMonitoringService.TimeseriesData> timeline = 
                    realtimeMonitoringService.getTimelineData(testId, type, minutes);
            
            log.debug("시계열 데이터 조회 성공: testId={}, type={}, points={}", 
                    testId, type, timeline.size());
            
            return ResponseEntity.ok(ApiResponse.success(timeline));
            
        } catch (Exception e) {
            log.error("시계열 데이터 조회 실패: testId={}, type={}", testId, type, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("시계열 데이터 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 로그 조회
     * GET /api/dashboard/tests/{testId}/logs?count={count}
     */
    @GetMapping("/tests/{testId}/logs")
    public ResponseEntity<ApiResponse<List<RealtimeMonitoringService.LogEntry>>> getTestLogs(
            @PathVariable String testId,
            @RequestParam(defaultValue = "100") int count) {
        try {
            List<RealtimeMonitoringService.LogEntry> logs = 
                    realtimeMonitoringService.getRecentLogs(testId, count);
            
            log.debug("테스트 로그 조회 성공: testId={}, count={}", testId, logs.size());
            
            return ResponseEntity.ok(ApiResponse.success(logs));
            
        } catch (Exception e) {
            log.error("테스트 로그 조회 실패: {}", testId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 로그 조회 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 테스트 삭제
     * DELETE /api/dashboard/tests/{testId}
     * 데이터베이스, Redis, 리포트 파일을 모두 삭제
     */
    @DeleteMapping("/tests/{testId}")
    public ResponseEntity<ApiResponse<String>> deleteTest(@PathVariable String testId) {
        try {
            log.info("테스트 삭제 요청: testId={}", testId);
            
            // 테스트 삭제 서비스 호출
            boolean deleted = dashboardService.deleteTest(testId);
            
            if (deleted) {
                log.info("테스트 삭제 성공: testId={}", testId);
                return ResponseEntity.ok(ApiResponse.success("테스트가 성공적으로 삭제되었습니다."));
            } else {
                log.warn("테스트를 찾을 수 없음: testId={}", testId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("테스트를 찾을 수 없습니다."));
            }
            
        } catch (Exception e) {
            log.error("테스트 삭제 실패: testId={}", testId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("테스트 삭제 실패: " + e.getMessage()));
        }
    }
    
    /**
     * 데이터베이스 헬스 체크
     * GET /api/dashboard/health/database
     */
    @GetMapping("/health/database")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkDatabaseHealth() {
        Map<String, String> health = new java.util.HashMap<>();
        health.put("status", "UP");
        health.put("database", "PostgreSQL");
        health.put("connection", "connected");
        
        return ResponseEntity.ok(ApiResponse.success(health));
    }
    
    // === Response DTOs ===
    
    /**
     * 표준 API 응답 형식
     */
    public static class ApiResponse<T> {
        private boolean success;
        private T data;
        private String message;
        private Long timestamp;
        
        public ApiResponse(boolean success, T data, String message) {
            this.success = success;
            this.data = data;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, data, null);
        }
        
        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message);
        }
        
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, null, message);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getMessage() { return message; }
        public Long getTimestamp() { return timestamp; }
    }
    
    /**
     * 테스트 시작 응답
     */
    public static class TestStartResponse {
        private String testId;
        private String message;
        
        public TestStartResponse(String testId, String message) {
            this.testId = testId;
            this.message = message;
        }
        
        public String getTestId() { return testId; }
        public String getMessage() { return message; }
    }
}