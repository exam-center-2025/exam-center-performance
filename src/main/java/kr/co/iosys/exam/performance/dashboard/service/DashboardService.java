package kr.co.iosys.exam.performance.dashboard.service;

import kr.co.iosys.exam.performance.dashboard.dto.TestRequest;
import kr.co.iosys.exam.performance.dashboard.dto.TestResult;
import kr.co.iosys.exam.performance.dashboard.dto.TestMetrics;
import kr.co.iosys.exam.performance.dashboard.entity.TestMetricsHistory;
import kr.co.iosys.exam.performance.dashboard.repository.TestResultQueryRepository;
import kr.co.iosys.exam.performance.dashboard.repository.TestMetricsQueryRepository;
import kr.co.iosys.exam.performance.dashboard.repository.TestMetricsHistoryRepository;
import kr.co.iosys.exam.performance.model.ExamPlan;
import kr.co.iosys.exam.performance.repository.ExamPlanRepository;
import kr.co.iosys.exam.performance.repository.PerformanceTestRepository;
import kr.co.iosys.exam.performance.service.GatlingRunnerService;
import kr.co.iosys.exam.performance.dto.PerformanceTestRequest;
import kr.co.iosys.exam.performance.dto.PerformanceTestResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 대시보드 핵심 서비스
 * 테스트 관리, 결과 조회, 실시간 데이터 처리
 * 
 * AIDEV-NOTE: PostgreSQL과 Redis를 함께 사용하는 하이브리드 아키텍처
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final TestResultQueryRepository testResultRepository;
    private final TestMetricsQueryRepository testMetricsRepository;
    private final TestMetricsHistoryRepository testMetricsHistoryRepository;
    private final PerformanceTestRepository performanceTestRepository;
    private final GatlingRunnerService gatlingRunnerService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ExamPlanRepository examPlanRepository;
    
    // Redis 키 패턴 상수
    private static final String REDIS_KEY_ACTIVE_TESTS = "tests:active";
    private static final String REDIS_KEY_CURRENT_METRICS = "metrics:current:%s";
    private static final String REDIS_KEY_TEST_STATUS = "test:status:%s";
    
    /**
     * 시험 계획 목록 조회
     */
    public List<ExamPlan> getExamPlans() {
        log.info("시험 계획 목록 조회");
        try {
            // 활성화된 시험 계획 조회
            List<ExamPlan> plans = examPlanRepository.findByEnableYn('Y');
            log.info("조회된 시험 계획 수: {}", plans.size());
            return plans;
        } catch (Exception e) {
            log.error("시험 계획 조회 실패", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 실행 중인 테스트 목록 조회 (Redis + Database)
     */
    public List<TestResult> getActiveTests() {
        log.info("실행 중인 테스트 목록 조회");
        try {
            // Redis에서 활성 테스트 ID 목록 조회
            Set<Object> activeTestIds = redisTemplate.opsForSet().members(REDIS_KEY_ACTIVE_TESTS);
            
            if (activeTestIds == null || activeTestIds.isEmpty()) {
                log.info("현재 실행 중인 테스트가 없습니다");
                return new ArrayList<>();
            }
            
            List<TestResult> activeTests = new ArrayList<>();
            for (Object testIdObj : activeTestIds) {
                String testId = testIdObj.toString();
                
                // Redis에서 현재 상태 조회
                String statusKey = String.format(REDIS_KEY_TEST_STATUS, testId);
                Object statusData = redisTemplate.opsForValue().get(statusKey);
                
                if (statusData != null) {
                    TestResult testResult = parseTestStatusFromRedis(testId, statusData.toString());
                    if (testResult != null) {
                        activeTests.add(testResult);
                    }
                }
            }
            
            log.info("실행 중인 테스트 {}개 조회 완료", activeTests.size());
            return activeTests;
            
        } catch (Exception e) {
            log.error("활성 테스트 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 최근 테스트 결과 조회
     */
    public List<TestResult> getRecentResults() {
        log.info("최근 테스트 결과 조회");
        try {
            List<Object[]> results = testResultRepository.findRecentResults();
            return convertToTestResults(results);
        } catch (Exception e) {
            log.error("최근 결과 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 모든 테스트 목록 조회 (실행 중 + 최근 완료)
     */
    public List<TestResult> getAllTests() {
        log.info("전체 테스트 목록 조회");
        try {
            // 실행 중인 테스트 조회
            List<TestResult> allTests = new ArrayList<>(getActiveTests());
            
            // 최근 완료된 테스트 추가
            List<TestResult> recentResults = getRecentResults();
            
            // 중복 제거를 위해 testId로 필터링
            Set<String> activeTestIds = allTests.stream()
                    .map(TestResult::getTestId)
                    .collect(java.util.stream.Collectors.toSet());
            
            for (TestResult result : recentResults) {
                if (!activeTestIds.contains(result.getTestId())) {
                    allTests.add(result);
                }
            }
            
            // 시작 시간 기준으로 정렬 (최신 순)
            allTests.sort((a, b) -> {
                if (b.getStartTime() == null) return -1;
                if (a.getStartTime() == null) return 1;
                return b.getStartTime().compareTo(a.getStartTime());
            });
            
            log.info("전체 테스트 {}개 조회 완료", allTests.size());
            return allTests;
            
        } catch (Exception e) {
            log.error("전체 테스트 목록 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 특정 테스트 결과 상세 조회
     */
    public Optional<TestResult> getTestResult(String testId) {
        log.info("테스트 결과 상세 조회: {}", testId);
        try {
            Optional<Object[]> result = testResultRepository.findByTestId(testId);
            return result.map(this::convertToTestResult);
        } catch (Exception e) {
            log.error("테스트 결과 조회 중 오류 발생: {}", testId, e);
            return Optional.empty();
        }
    }
    
    /**
     * 테스트 시작
     */
    public CompletableFuture<String> startTest(TestRequest request) {
        log.info("성능 테스트 시작: plan={}, users={}, duration={}초", 
                request.getPlanId(), request.getMaxUsers(), request.getTestDurationSeconds());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TestRequest를 PerformanceTestRequest로 변환
                PerformanceTestRequest performanceRequest = new PerformanceTestRequest();
                performanceRequest.setPlanId(request.getPlanId());
                performanceRequest.setRunType(request.getRunType());
                performanceRequest.setMaxUsers(request.getMaxUsers());
                performanceRequest.setTestName(request.getTestName() != null ? request.getTestName() : "Dashboard_Test_" + System.currentTimeMillis());
                performanceRequest.setScenario(request.getScenario());
                performanceRequest.setRampUpDurationSeconds(request.getRampUpSeconds());
                performanceRequest.setTestDurationSeconds(request.getTestDurationSeconds());
                
                // Gatling 테스트 실행
                PerformanceTestResponse response = gatlingRunnerService.startPerformanceTest(performanceRequest);
                String testId = response.getTestId();
                
                // Redis에 활성 테스트로 등록
                redisTemplate.opsForSet().add(REDIS_KEY_ACTIVE_TESTS, testId);
                
                // 테스트 상태 초기화
                initializeTestStatus(testId, request);
                
                log.info("성능 테스트 시작 완료: {}", testId);
                return testId;
                
            } catch (Exception e) {
                log.error("성능 테스트 시작 실패", e);
                throw new RuntimeException("테스트 시작 실패: " + e.getMessage());
            }
        });
    }
    
    /**
     * 테스트 중단
     */
    public boolean stopTest(String testId) {
        log.info("테스트 중단 요청: {}", testId);
        try {
            // Gatling 프로세스 중단
            gatlingRunnerService.cancelTest(testId);
            
            // Redis에서 활성 테스트 목록에서 제거
            redisTemplate.opsForSet().remove(REDIS_KEY_ACTIVE_TESTS, testId);
            
            // 테스트 상태 업데이트
            updateTestStatus(testId, "CANCELLED", "사용자에 의해 중단됨");
            
            log.info("테스트 중단 완료: {}", testId);
            return true;
            
        } catch (Exception e) {
            log.error("테스트 중단 실패: {}", testId, e);
            return false;
        }
    }
    
    /**
     * 실시간 메트릭 조회 (Redis)
     */
    public Optional<TestMetrics> getCurrentMetrics(String testId) {
        try {
            String metricsKey = String.format(REDIS_KEY_CURRENT_METRICS, testId);
            Object metricsData = redisTemplate.opsForValue().get(metricsKey);
            
            if (metricsData != null) {
                TestMetrics metrics = objectMapper.readValue(metricsData.toString(), TestMetrics.class);
                return Optional.of(metrics);
            }
            
        } catch (Exception e) {
            log.error("실시간 메트릭 조회 실패: {}", testId, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * 테스트 메트릭 히스토리 조회
     */
    public List<TestMetrics> getMetricsHistory(String testId) {
        log.info("테스트 메트릭 히스토리 조회: {}", testId);
        try {
            List<TestMetricsHistory> historyList = testMetricsHistoryRepository.findByTestIdOrderByTimestamp(testId);
            
            // TestMetricsHistory 엔티티를 TestMetrics DTO로 변환
            List<TestMetrics> metricsList = new ArrayList<>();
            for (TestMetricsHistory history : historyList) {
                TestMetrics metrics = TestMetrics.builder()
                        .testId(history.getTestId())
                        .timestamp(history.getTimestamp() != null ? 
                                 history.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                        .activeUsers(history.getActiveUsers())
                        .tps(history.getTps() != null ? history.getTps().doubleValue() : null)
                        .avgResponseTime(history.getAvgResponseTime() != null ? history.getAvgResponseTime().doubleValue() : null)
                        .minResponseTime(history.getMinResponseTime() != null ? history.getMinResponseTime().doubleValue() : null)
                        .maxResponseTime(history.getMaxResponseTime() != null ? history.getMaxResponseTime().doubleValue() : null)
                        .p95ResponseTime(history.getP95ResponseTime() != null ? history.getP95ResponseTime().doubleValue() : null)
                        .p99ResponseTime(history.getP99ResponseTime() != null ? history.getP99ResponseTime().doubleValue() : null)
                        .successCount(history.getSuccessCount())
                        .errorCount(history.getErrorCount())
                        .errorRate(history.getErrorRate() != null ? history.getErrorRate().doubleValue() : null)
                        .build();
                metricsList.add(metrics);
            }
            
            // 최대 300개만 반환 (최신 데이터)
            if (metricsList.size() > 300) {
                return metricsList.subList(metricsList.size() - 300, metricsList.size());
            }
            
            return metricsList;
        } catch (Exception e) {
            log.error("메트릭 히스토리 조회 실패: {}", testId, e);
            return new ArrayList<>();
        }
    }
    
    // === Private Methods ===
    
    /**
     * Redis 테스트 상태 데이터를 TestResult로 변환
     */
    private TestResult parseTestStatusFromRedis(String testId, String statusData) {
        try {
            // JSON 파싱하여 TestResult 객체 생성
            Map<String, Object> statusMap = objectMapper.readValue(statusData, Map.class);
            
            return TestResult.builder()
                    .testId(testId)
                    .status((String) statusMap.get("status"))
                    .testName((String) statusMap.getOrDefault("testName", "Performance Test"))
                    .planId(statusMap.get("planId") != null ? 
                           ((Number) statusMap.get("planId")).longValue() : null)
                    .startTime(statusMap.get("startTime") != null ? 
                              LocalDateTime.ofInstant(
                                  java.time.Instant.ofEpochMilli(((Number) statusMap.get("startTime")).longValue()),
                                  java.time.ZoneId.systemDefault()) : null)
                    .maxConcurrentUsers(statusMap.get("targetUsers") != null ? 
                                       ((Number) statusMap.get("targetUsers")).intValue() : null)
                    .build();
        } catch (Exception e) {
            log.error("Redis 상태 데이터 파싱 실패: {}", testId, e);
            return null;
        }
    }
    
    /**
     * 테스트 상태 초기화
     */
    private void initializeTestStatus(String testId, TestRequest request) {
        try {
            String statusKey = String.format(REDIS_KEY_TEST_STATUS, testId);
            
            // 상태 정보 JSON 생성
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("testId", testId);
            statusMap.put("status", "RUNNING");
            statusMap.put("startTime", System.currentTimeMillis());
            statusMap.put("currentUsers", 0);
            statusMap.put("targetUsers", request.getMaxUsers());
            statusMap.put("progress", 0.0);
            statusMap.put("message", "테스트 시작 중...");
            
            String statusJson = objectMapper.writeValueAsString(statusMap);
            
            // Redis에 저장 (1시간 TTL)
            redisTemplate.opsForValue().set(statusKey, statusJson, 
                java.time.Duration.ofHours(1));
                
        } catch (JsonProcessingException e) {
            log.error("테스트 상태 초기화 실패: {}", testId, e);
        }
    }
    
    /**
     * 테스트 상태 업데이트
     */
    private void updateTestStatus(String testId, String status, String message) {
        try {
            String statusKey = String.format(REDIS_KEY_TEST_STATUS, testId);
            
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("testId", testId);
            statusMap.put("status", status);
            statusMap.put("message", message);
            statusMap.put("updatedAt", System.currentTimeMillis());
            
            String statusJson = objectMapper.writeValueAsString(statusMap);
            
            redisTemplate.opsForValue().set(statusKey, statusJson, 
                java.time.Duration.ofHours(1));
                
        } catch (JsonProcessingException e) {
            log.error("테스트 상태 업데이트 실패: {}", testId, e);
        }
    }
    
    /**
     * Object[] 배열을 TestResult 리스트로 변환
     */
    private List<TestResult> convertToTestResults(List<Object[]> results) {
        List<TestResult> testResults = new ArrayList<>();
        
        for (Object[] result : results) {
            TestResult testResult = convertToTestResult(result);
            if (testResult != null) {
                testResults.add(testResult);
            }
        }
        
        return testResults;
    }
    
    /**
     * Object[] 배열을 TestResult 객체로 변환
     */
    private TestResult convertToTestResult(Object[] result) {
        try {
            return TestResult.builder()
                    .testId((String) result[0])
                    .testName((String) result[1])
                    .planId(((Number) result[2]).longValue())
                    .status((String) result[3])
                    .startTime(result[4] != null ? ((Timestamp) result[4]).toLocalDateTime() : null)
                    .endTime(result[5] != null ? ((Timestamp) result[5]).toLocalDateTime() : null)
                    .totalRequests(result[6] != null ? ((Number) result[6]).longValue() : null)
                    .successfulRequests(result[7] != null ? ((Number) result[7]).longValue() : null)
                    .failedRequests(result[8] != null ? ((Number) result[8]).longValue() : null)
                    .successRate(result[9] != null ? ((Number) result[9]).doubleValue() : null)
                    .avgResponseTime(result[10] != null ? ((Number) result[10]).doubleValue() : null)
                    .minResponseTime(result[11] != null ? ((Number) result[11]).doubleValue() : null)
                    .maxResponseTime(result[12] != null ? ((Number) result[12]).doubleValue() : null)
                    .p50ResponseTime(result[13] != null ? ((Number) result[13]).doubleValue() : null)
                    .p75ResponseTime(result[14] != null ? ((Number) result[14]).doubleValue() : null)
                    .p95ResponseTime(result[15] != null ? ((Number) result[15]).doubleValue() : null)
                    .p99ResponseTime(result[16] != null ? ((Number) result[16]).doubleValue() : null)
                    .maxTps(result[17] != null ? ((Number) result[17]).doubleValue() : null)
                    .avgTps(result[18] != null ? ((Number) result[18]).doubleValue() : null)
                    .testDurationSeconds(result[19] != null ? ((Number) result[19]).intValue() : null)
                    .maxConcurrentUsers(result[20] != null ? ((Number) result[20]).intValue() : null)
                    .reportPath((String) result[21])
                    .errorMessage((String) result[22])
                    .build();
        } catch (Exception e) {
            log.error("TestResult 변환 실패", e);
            return null;
        }
    }
    
    /**
     * Object[] 배열을 TestMetrics 리스트로 변환
     */
    private List<TestMetrics> convertToTestMetrics(List<Object[]> results) {
        List<TestMetrics> metrics = new ArrayList<>();
        
        for (Object[] result : results) {
            try {
                TestMetrics metric = TestMetrics.builder()
                        .testId((String) result[0])
                        .timestamp(result[1] != null ? ((Number) result[1]).longValue() : null)
                        .activeUsers(result[2] != null ? ((Number) result[2]).intValue() : null)
                        .tps(result[3] != null ? ((Number) result[3]).doubleValue() : null)
                        .avgResponseTime(result[4] != null ? ((Number) result[4]).doubleValue() : null)
                        .minResponseTime(result[5] != null ? ((Number) result[5]).doubleValue() : null)
                        .maxResponseTime(result[6] != null ? ((Number) result[6]).doubleValue() : null)
                        .p95ResponseTime(result[7] != null ? ((Number) result[7]).doubleValue() : null)
                        .p99ResponseTime(result[8] != null ? ((Number) result[8]).doubleValue() : null)
                        .successCount(result[9] != null ? ((Number) result[9]).longValue() : null)
                        .errorCount(result[10] != null ? ((Number) result[10]).longValue() : null)
                        .errorRate(result[11] != null ? ((Number) result[11]).doubleValue() : null)
                        .build();
                        
                metrics.add(metric);
                
            } catch (Exception e) {
                log.error("TestMetrics 변환 실패", e);
            }
        }
        
        return metrics;
    }
    
    /**
     * 시스템 설정 정보 조회
     * AIDEV-NOTE: Vue 하이브리드 대시보드용
     */
    public Map<String, Object> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxConcurrentTests", 3);
        config.put("defaultMaxUsers", 100);
        config.put("defaultRampUpDuration", 60);
        config.put("defaultTestDuration", 300);
        config.put("gatlingResultsPath", "./gatling-results");
        return config;
    }
    
    /**
     * 현재 사용자 정보 조회
     * AIDEV-NOTE: Vue 하이브리드 대시보드용
     */
    public Map<String, Object> getCurrentUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", "admin");
        user.put("role", "ADMIN");
        user.put("lastLogin", System.currentTimeMillis());
        return user;
    }
    
    /**
     * 테스트 삭제
     * 데이터베이스, Redis, 리포트 파일 삭제
     */
    @Transactional
    public boolean deleteTest(String testId) {
        log.info("테스트 삭제 시작: {}", testId);
        
        try {
            // 1. 데이터베이스에서 테스트 정보 조회
            Optional<Object[]> result = testResultRepository.findByTestId(testId);
            if (!result.isPresent()) {
                log.warn("삭제할 테스트를 찾을 수 없음: {}", testId);
                return false;
            }
            
            Object[] data = result.get();
            String reportPath = (String) data[21]; // report_path 컬럼
            
            // 2. Redis에서 관련 데이터 삭제
            deleteRedisData(testId);
            
            // 3. 리포트 파일 삭제
            if (reportPath != null && !reportPath.isEmpty()) {
                deleteReportFiles(reportPath);
            }
            
            // 4. 데이터베이스에서 삭제
            // performance_tests 테이블 삭제 (CASCADE로 관련 테이블도 삭제됨)
            performanceTestRepository.deleteById(testId);
            
            log.info("테스트 삭제 완료: {}", testId);
            return true;
            
        } catch (Exception e) {
            log.error("테스트 삭제 실패: {}", testId, e);
            throw new RuntimeException("테스트 삭제 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * Redis에서 테스트 관련 데이터 삭제
     */
    private void deleteRedisData(String testId) {
        try {
            // 활성 테스트 목록에서 제거
            redisTemplate.opsForSet().remove(REDIS_KEY_ACTIVE_TESTS, testId);
            
            // 테스트 메트릭 데이터 삭제
            String metricsKey = "test:metrics:" + testId;
            redisTemplate.delete(metricsKey);
            
            // 실시간 데이터 삭제
            String realtimeKey = "test:realtime:" + testId;
            redisTemplate.delete(realtimeKey);
            
            // 테스트 상태 데이터 삭제
            String statusKey = "test:status:" + testId;
            redisTemplate.delete(statusKey);
            
            log.info("Redis 데이터 삭제 완료: {}", testId);
        } catch (Exception e) {
            log.error("Redis 데이터 삭제 실패: {}", testId, e);
        }
    }
    
    /**
     * 리포트 파일 삭제
     */
    private void deleteReportFiles(String reportPath) {
        try {
            java.nio.file.Path path = null;
            
            if (reportPath.startsWith("build/reports/gatling/")) {
                // build/reports/gatling/test-xxx 형식
                path = java.nio.file.Paths.get(reportPath);
            } else if (reportPath.startsWith("local-reports/")) {
                // local-reports/path 형식
                path = java.nio.file.Paths.get(reportPath);
            } else if (!reportPath.startsWith("/")) {
                // 상대 경로인 경우
                path = java.nio.file.Paths.get("build/reports/gatling", reportPath);
            }
            
            if (path != null && java.nio.file.Files.exists(path)) {
                // 디렉토리와 하위 파일 모두 삭제
                java.nio.file.Files.walk(path)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(java.io.File::delete);
                    
                log.info("리포트 파일 삭제 완료: {}", reportPath);
            } else {
                log.warn("리포트 파일이 존재하지 않음: {}", reportPath);
            }
        } catch (Exception e) {
            log.error("리포트 파일 삭제 실패: {}", reportPath, e);
        }
    }
    
    /**
     * 테스트 리포트 URL 조회
     * Gatling 리포트 경로 반환
     */
    public String getReportUrl(String testId) {
        log.info("테스트 리포트 URL 조회: {}", testId);
        try {
            Optional<Object[]> result = testResultRepository.findByTestId(testId);
            if (result.isPresent()) {
                Object[] data = result.get();
                // result_path는 인덱스 21 (reportPath)
                String reportPath = (String) data[21];
                if (reportPath != null && !reportPath.isEmpty()) {
                    // AIDEV-NOTE: Gatling 리포트 경로를 올바른 URL로 변환
                    String fullUrl;
                    if (reportPath.startsWith("build/reports/gatling/")) {
                        // build/reports/gatling/test-xxx -> /performance/reports/gatling/test-xxx/index.html
                        String gatlingPath = reportPath.substring("build/reports/gatling/".length());
                        fullUrl = "/performance/reports/gatling/" + gatlingPath + "/index.html";
                    } else if (reportPath.startsWith("local-reports/")) {
                        // local-reports/path -> /performance/reports/path/index.html
                        fullUrl = "/performance/reports/" + reportPath.substring("local-reports/".length()) + "/index.html";
                    } else if (reportPath.startsWith("/")) {
                        // 절대 경로인 경우 그대로 사용
                        fullUrl = reportPath;
                    } else {
                        // 상대 경로인 경우 reports 경로 추가
                        fullUrl = "/performance/reports/" + reportPath + "/index.html";
                    }
                    log.info("리포트 경로 조회 완료: {} -> {}", reportPath, fullUrl);
                    return fullUrl;
                } else {
                    log.warn("리포트 경로가 비어있음: {}", testId);
                    return null;
                }
            } else {
                log.warn("테스트 결과를 찾을 수 없음: {}", testId);
                return null;
            }
        } catch (Exception e) {
            log.error("리포트 URL 조회 실패: {}", testId, e);
            return null;
        }
    }
    
    /**
     * 테스트의 rampUpSeconds 값 조회
     * AIDEV-NOTE: performance_tests 테이블에서 실제 ramp_up_seconds 값을 가져옴
     * 
     * @param testId 테스트 ID
     * @return rampUpSeconds 값 (없으면 null)
     */
    public Integer getRampUpSeconds(String testId) {
        try {
            return performanceTestRepository.findById(testId)
                    .map(test -> test.getRampUpSeconds())
                    .orElse(null);
        } catch (Exception e) {
            log.error("rampUpSeconds 조회 실패: {}", testId, e);
            return null;
        }
    }
    
    /**
     * 테스트의 maxUsers 값 조회
     * AIDEV-NOTE: performance_tests 테이블에서 실제 max_users 값을 가져옴
     * 
     * @param testId 테스트 ID
     * @return maxUsers 값 (없으면 null)
     */
    public Integer getMaxUsers(String testId) {
        try {
            return performanceTestRepository.findById(testId)
                    .map(test -> test.getMaxUsers())
                    .orElse(null);
        } catch (Exception e) {
            log.error("maxUsers 조회 실패: {}", testId, e);
            return null;
        }
    }
}