package kr.co.iosys.exam.performance.dashboard.controller;

import kr.co.iosys.exam.performance.dashboard.dto.TestRequest;
import kr.co.iosys.exam.performance.dashboard.dto.TestResult;
import kr.co.iosys.exam.performance.dashboard.dto.TestMetrics;
import kr.co.iosys.exam.performance.dashboard.service.DashboardService;
import kr.co.iosys.exam.performance.model.ExamPlan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 대시보드 Thymeleaf 컨트롤러
 * 대시보드 페이지 렌더링 및 모델 데이터 제공
 * 
 * AIDEV-NOTE: Thymeleaf 템플릿 렌더링 전용, REST API는 별도 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 메인 대시보드 페이지 - Vue 버전으로 완전 전환
     * GET /dashboard
     * AIDEV-NOTE: 2025-01-31 Vue 3로 완전 전환
     */
    @GetMapping
    public String dashboard(Model model) {
        log.info("대시보드 페이지 요청 - Vue 버전으로 전환");
        
        try {
            // 초기 데이터 설정 (Vue에서 사용)
            model.addAttribute("config", dashboardService.getSystemConfig());
            model.addAttribute("user", dashboardService.getCurrentUser());
            
            log.info("Vue 대시보드 초기 데이터 로드 완료");
        } catch (Exception e) {
            log.error("Vue 대시보드 초기 데이터 로드 실패", e);
            model.addAttribute("errorMessage", "초기 데이터 로드 중 오류가 발생했습니다.");
        }
        
        return "dashboard/vue-dashboard";
    }
    
    
    /**
     * 테스트 설정 페이지 - Vue 버전으로 완전 전환
     * GET /dashboard/configure
     * AIDEV-NOTE: 2025-01-31 Vue 3로 완전 전환
     */
    @GetMapping("/configure")
    public String configure(@RequestParam(required = false) Long planId, Model model) {
        log.info("테스트 설정 페이지 요청 - Vue 버전으로 전환 - planId: {}", planId);
        
        try {
            // 시험 계획 목록
            List<ExamPlan> examPlans = dashboardService.getExamPlans();
            model.addAttribute("examPlans", examPlans);
            
            // 선택된 계획이 있으면 설정
            if (planId != null) {
                ExamPlan selectedPlan = examPlans.stream()
                        .filter(plan -> plan.getPlanId().equals(planId))
                        .findFirst()
                        .orElse(null);
                model.addAttribute("selectedPlan", selectedPlan);
            }
            
            // 기본 테스트 요청 객체
            TestRequest defaultRequest = TestRequest.builder()
                    .planId(planId)
                    .runType("TEST")
                    .maxUsers(100)
                    .rampUpSeconds(60)
                    .testDurationSeconds(300)
                    .scenario("COMPLETE")
                    .build();
            model.addAttribute("testRequest", defaultRequest);
            
            // 시나리오 옵션
            model.addAttribute("scenarios", List.of(
                    new ScenarioOption("BASIC", "기본 테스트", "핵심 기능만 테스트하는 가벼운 시나리오"),
                    new ScenarioOption("COMPLETE", "완전 테스트", "모든 기능을 포함하는 포괄적인 시나리오"),
                    new ScenarioOption("STRESS", "스트레스 테스트", "시스템 한계를 테스트하는 고강도 시나리오")
            ));
            
            // 실행 타입 옵션
            // AIDEV-NOTE: Vue 프론트엔드와 일치하도록 REAL 사용 (PROD 대신)
            model.addAttribute("runTypes", List.of(
                    new RunTypeOption("TEST", "테스트 환경"),
                    new RunTypeOption("REAL", "실제 환경")
            ));
            
            // Vue 앱을 위한 초기 설정
            model.addAttribute("vueConfig", Map.of(
                "apiEndpoint", "/api/performance/dashboard",
                "wsEndpoint", "/ws",
                "enableTemplates", true,
                "maxUsers", 10000,
                "maxDuration", 7200
            ));
            
            // 사용자 정보
            model.addAttribute("user", Map.of(
                "name", "Administrator",
                "role", "ADMIN"
            ));
            
            // 페이지 정보
            model.addAttribute("pageTitle", "테스트 설정");
            model.addAttribute("currentPage", "configure");
            
        } catch (Exception e) {
            log.error("Vue 테스트 설정 페이지 로드 실패", e);
            model.addAttribute("errorMessage", "페이지 로드 중 오류가 발생했습니다.");
        }
        
        return "dashboard/configure-vue";
    }
    
    
    /**
     * 실시간 모니터링 페이지 - Vue 버전으로 완전 전환
     * GET /dashboard/monitor/{testId}
     * AIDEV-NOTE: 2025-01-31 Vue 3로 완전 전환
     */
    @GetMapping("/monitor/{testId}")
    public String monitor(@PathVariable String testId, Model model) {
        log.info("실시간 모니터링 페이지 요청 - Vue 버전으로 전환 - testId: {}", testId);
        
        try {
            // 테스트 정보 조회
            Optional<TestResult> testResult = dashboardService.getTestResult(testId);
            if (testResult.isEmpty()) {
                log.warn("테스트를 찾을 수 없음: {}", testId);
                model.addAttribute("errorMessage", "테스트를 찾을 수 없습니다.");
                model.addAttribute("testId", testId);
                return "dashboard/monitor-vue";
            }
            
            TestResult result = testResult.get();
            model.addAttribute("testResult", result);
            model.addAttribute("testId", testId);
            
            // Vue 앱을 위한 초기 설정 - 안전한 기본값 사용
            model.addAttribute("testConfig", Map.of(
                "testName", result.getTestName() != null ? result.getTestName() : testId,
                "planId", result.getPlanId(),
                "maxUsers", 100, // 기본값
                "rampUpDurationSeconds", 60, // 기본값
                "testDurationSeconds", result.getTestDurationSeconds() != null ? result.getTestDurationSeconds() : 300,
                "runType", "TEST", // 기본값
                "scenario", "NORMAL_USER" // 기본값
            ));
            
            // 시스템 설정
            model.addAttribute("systemConfig", Map.of(
                "refreshInterval", 1000,
                "maxDataPoints", 300,
                "wsEndpoint", "/ws",
                "apiEndpoint", "/api/dashboard"
            ));
            
            // 사용자 정보 (선택사항)
            model.addAttribute("user", Map.of(
                "name", "Administrator",
                "role", "ADMIN"
            ));
            
            // 페이지 정보
            model.addAttribute("pageTitle", "실시간 모니터링 - " + testId);
            model.addAttribute("currentPage", "monitor");
            
        } catch (Exception e) {
            log.error("Vue 모니터링 페이지 로드 실패: {}", testId, e);
            model.addAttribute("errorMessage", "모니터링 페이지 로드 중 오류가 발생했습니다.");
            model.addAttribute("testId", testId);
        }
        
        return "dashboard/monitor-vue";
    }
    
    
    /**
     * 테스트 결과 페이지 - Vue 버전으로 완전 전환
     * GET /dashboard/results/{testId}
     * AIDEV-NOTE: 2025-01-31 Vue 3로 완전 전환
     */
    @GetMapping("/results/{testId}")
    public String results(@PathVariable String testId, Model model) {
        log.info("테스트 결과 페이지 요청 - Vue 버전으로 전환 - testId: {}", testId);
        
        try {
            // 테스트 결과 조회
            Optional<TestResult> testResult = dashboardService.getTestResult(testId);
            if (testResult.isEmpty()) {
                log.warn("테스트 결과를 찾을 수 없음: {}", testId);
                model.addAttribute("errorMessage", "테스트 결과를 찾을 수 없습니다.");
                model.addAttribute("testId", testId);
                // 빈 TestResult 객체 제공
                model.addAttribute("testResult", TestResult.builder()
                    .testId(testId)
                    .status("NOT_FOUND")
                    .build());
                model.addAttribute("metricsHistory", new ArrayList<>());
                model.addAttribute("summary", ResultSummary.builder()
                    .testId(testId)
                    .duration(0L)
                    .totalRequests(0L)
                    .successRate(0.0)
                    .avgResponseTime(0.0)
                    .maxTps(0.0)
                    .maxConcurrentUsers(0)
                    .isSuccessful(false)
                    .build());
                return "dashboard/results";
            }
            
            TestResult result = testResult.get();
            model.addAttribute("testResult", result);
            
            // 메트릭 히스토리 조회 (차트용) - null 체크
            List<TestMetrics> metricsHistory = dashboardService.getMetricsHistory(testId);
            model.addAttribute("metricsHistory", metricsHistory != null ? metricsHistory : new ArrayList<>());
            
            // 결과 요약 계산 - null 체크
            ResultSummary summary = createResultSummary(result);
            model.addAttribute("summary", summary != null ? summary : ResultSummary.builder()
                .testId(testId)
                .duration(0L)
                .totalRequests(0L)
                .successRate(0.0)
                .avgResponseTime(0.0)
                .maxTps(0.0)
                .maxConcurrentUsers(0)
                .isSuccessful(false)
                .build());
            
            // Gatling 리포트 링크
            if (result.getReportPath() != null) {
                // result.getReportPath()는 "build/reports/gatling/examcentersimulation-20250831001719929" 형태
                String reportPath = result.getReportPath();
                // "build/reports/gatling/" 부분 제거하여 실제 폴더명만 추출
                if (reportPath.startsWith("build/reports/gatling/")) {
                    reportPath = reportPath.substring("build/reports/gatling/".length());
                }
                // /performance/reports/gatling/{folder}/index.html 형태로 URL 생성
                model.addAttribute("reportUrl", "/performance/reports/gatling/" + reportPath + "/index.html");
                log.info("Gatling 리포트 URL 생성: {}", "/performance/reports/gatling/" + reportPath + "/index.html");
            } else {
                model.addAttribute("reportUrl", null);
            }
            
            // 페이지 정보
            model.addAttribute("pageTitle", "테스트 결과 - " + testId);
            model.addAttribute("currentPage", "results");
            
        } catch (Exception e) {
            log.error("테스트 결과 페이지 로드 실패: {}", testId, e);
            model.addAttribute("errorMessage", "테스트 결과 로드 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("testId", testId);
            // 오류 시에도 기본 객체 제공
            model.addAttribute("testResult", TestResult.builder()
                .testId(testId)
                .status("ERROR")
                .build());
            model.addAttribute("metricsHistory", new ArrayList<>());
            model.addAttribute("summary", ResultSummary.builder()
                .testId(testId)
                .duration(0L)
                .totalRequests(0L)
                .successRate(0.0)
                .avgResponseTime(0.0)
                .maxTps(0.0)
                .maxConcurrentUsers(0)
                .isSuccessful(false)
                .build());
        }
        
        return "dashboard/results-vue";
    }
    
    
    
    // === Private Methods ===
    
    /**
     * 테스트 결과 요약 생성
     */
    private ResultSummary createResultSummary(TestResult result) {
        return ResultSummary.builder()
                .testId(result.getTestId())
                .duration(result.getActualDurationSeconds() != null ? 
                         result.getActualDurationSeconds() : result.getTestDurationSeconds())
                .totalRequests(result.getTotalRequests() != null ? result.getTotalRequests() : 0L)
                .successRate(result.getSuccessRate() != null ? result.getSuccessRate() : 0.0)
                .avgResponseTime(result.getAvgResponseTime() != null ? result.getAvgResponseTime() : 0.0)
                .maxTps(result.getMaxTps() != null ? result.getMaxTps() : 0.0)
                .maxConcurrentUsers(result.getMaxConcurrentUsers() != null ? result.getMaxConcurrentUsers() : 0)
                .isSuccessful(result.isSuccessful())
                .build();
    }
    
    // === Inner Classes for Model Attributes ===
    
    public static class ScenarioOption {
        private String value;
        private String label;
        private String description;
        
        public ScenarioOption(String value, String label, String description) {
            this.value = value;
            this.label = label;
            this.description = description;
        }
        
        public String getValue() { return value; }
        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }
    
    public static class RunTypeOption {
        private String value;
        private String label;
        
        public RunTypeOption(String value, String label) {
            this.value = value;
            this.label = label;
        }
        
        public String getValue() { return value; }
        public String getLabel() { return label; }
    }
    
    public static class ResultSummary {
        private String testId;
        private Long duration;
        private Long totalRequests;
        private Double successRate;
        private Double avgResponseTime;
        private Double maxTps;
        private Integer maxConcurrentUsers;
        private Boolean isSuccessful;
        
        public static ResultSummaryBuilder builder() {
            return new ResultSummaryBuilder();
        }
        
        // Getters
        public String getTestId() { return testId; }
        public Long getDuration() { return duration; }
        public Long getTotalRequests() { return totalRequests; }
        public Double getSuccessRate() { return successRate; }
        public Double getAvgResponseTime() { return avgResponseTime; }
        public Double getMaxTps() { return maxTps; }
        public Integer getMaxConcurrentUsers() { return maxConcurrentUsers; }
        public Boolean getIsSuccessful() { return isSuccessful; }
        
        // Builder class
        public static class ResultSummaryBuilder {
            private String testId;
            private Long duration;
            private Long totalRequests;
            private Double successRate;
            private Double avgResponseTime;
            private Double maxTps;
            private Integer maxConcurrentUsers;
            private Boolean isSuccessful;
            
            public ResultSummaryBuilder testId(String testId) { this.testId = testId; return this; }
            public ResultSummaryBuilder duration(Long duration) { this.duration = duration; return this; }
            public ResultSummaryBuilder totalRequests(Long totalRequests) { this.totalRequests = totalRequests; return this; }
            public ResultSummaryBuilder successRate(Double successRate) { this.successRate = successRate; return this; }
            public ResultSummaryBuilder avgResponseTime(Double avgResponseTime) { this.avgResponseTime = avgResponseTime; return this; }
            public ResultSummaryBuilder maxTps(Double maxTps) { this.maxTps = maxTps; return this; }
            public ResultSummaryBuilder maxConcurrentUsers(Integer maxConcurrentUsers) { this.maxConcurrentUsers = maxConcurrentUsers; return this; }
            public ResultSummaryBuilder isSuccessful(Boolean isSuccessful) { this.isSuccessful = isSuccessful; return this; }
            
            public ResultSummary build() {
                ResultSummary summary = new ResultSummary();
                summary.testId = this.testId;
                summary.duration = this.duration;
                summary.totalRequests = this.totalRequests;
                summary.successRate = this.successRate;
                summary.avgResponseTime = this.avgResponseTime;
                summary.maxTps = this.maxTps;
                summary.maxConcurrentUsers = this.maxConcurrentUsers;
                summary.isSuccessful = this.isSuccessful;
                return summary;
            }
        }
    }
}