package kr.co.iosys.exam.performance.service;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import kr.co.iosys.exam.performance.config.PerformanceTestProperties;
import kr.co.iosys.exam.performance.dto.PerformanceTestRequest;
import kr.co.iosys.exam.performance.dto.PerformanceTestResponse;
import kr.co.iosys.exam.performance.exception.PerformanceTestException;
// import kr.co.iosys.exam.performance.simulation.DynamicExamSimulation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AIDEV-NOTE: Gatling 시뮬레이션 실행 관리 서비스
 * 프로그래매틱하게 Gatling 성능 테스트를 실행하고 결과를 관리
 */
@Slf4j
@Service
public class GatlingRunnerService {

    private final PerformanceTestProperties properties;
    private final TestConfigurationService configurationService;
    private final DatabaseService databaseService;

    // 실행 중인 테스트 추적을 위한 맵
    private final Map<String, PerformanceTestResponse> runningTests = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    
    // AIDEV-NOTE: 생성자에서 ExecutorService 초기화
    public GatlingRunnerService(PerformanceTestProperties properties, 
                               TestConfigurationService configurationService,
                               DatabaseService databaseService) {
        this.properties = properties;
        this.configurationService = configurationService;
        this.databaseService = databaseService;
        this.executorService = Executors.newFixedThreadPool(
                properties.getGatling().getMaxConcurrentTests());
    }

    /**
     * 성능 테스트 시작
     */
    public PerformanceTestResponse startPerformanceTest(PerformanceTestRequest request) {
        // 동시 실행 제한 확인
        if (runningTests.size() >= properties.getGatling().getMaxConcurrentTests()) {
            throw new PerformanceTestException(
                    "동시 실행 가능한 테스트 수를 초과했습니다. 현재 실행 중: " + runningTests.size(),
                    "CONCURRENT_LIMIT_EXCEEDED");
        }

        String testId = configurationService.generateTestId(request);
        
        // 초기 응답 생성
        PerformanceTestResponse response = PerformanceTestResponse.builder()
                .testId(testId)
                .testName(request.getTestName())
                .status(PerformanceTestResponse.TestStatus.PENDING)
                .startTime(LocalDateTime.now())
                .planId(request.getPlanId())
                .maxUsers(request.getMaxUsers())
                .scenario(request.getScenario())
                .build();

        // 실행 중인 테스트 맵에 추가
        runningTests.put(testId, response);

        // 비동기로 테스트 실행
        executeTestAsync(testId, request);

        log.info("성능 테스트 시작 요청 접수: {} (ID: {})", request.getTestName(), testId);
        return response;
    }

    /**
     * 테스트 상태 조회
     */
    public PerformanceTestResponse getTestStatus(String testId) {
        PerformanceTestResponse response = runningTests.get(testId);
        if (response == null) {
            throw new PerformanceTestException(
                    "테스트를 찾을 수 없습니다: " + testId,
                    "TEST_NOT_FOUND");
        }
        return response;
    }

    /**
     * 테스트 중단
     */
    public PerformanceTestResponse cancelTest(String testId) {
        PerformanceTestResponse response = runningTests.get(testId);
        if (response == null) {
            throw new PerformanceTestException(
                    "테스트를 찾을 수 없습니다: " + testId,
                    "TEST_NOT_FOUND");
        }

        if (response.getStatus() == PerformanceTestResponse.TestStatus.RUNNING) {
            // AIDEV-NOTE: 실제 Gatling 프로세스 중단은 복잡하므로 상태만 변경
            response.setStatus(PerformanceTestResponse.TestStatus.CANCELLED);
            response.setEndTime(LocalDateTime.now());
            log.info("성능 테스트 중단 요청: {}", testId);
        }

        return response;
    }

    /**
     * 실행 중인 모든 테스트 조회
     */
    public Map<String, PerformanceTestResponse> getAllTests() {
        return Map.copyOf(runningTests);
    }

    /**
     * 비동기 테스트 실행
     */
    @Async
    private void executeTestAsync(String testId, PerformanceTestRequest request) {
        CompletableFuture.runAsync(() -> {
            try {
                executeGatlingTest(testId, request);
            } catch (Exception e) {
                log.error("성능 테스트 실행 실패: {}", testId, e);
                updateTestStatus(testId, PerformanceTestResponse.TestStatus.FAILED, e.getMessage());
            }
        }, executorService);
    }

    /**
     * Gatling 테스트 실행
     */
    private void executeGatlingTest(String testId, PerformanceTestRequest request) {
        try {
            log.info("Gatling 테스트 실행 시작: {}", testId);
            updateTestStatus(testId, PerformanceTestResponse.TestStatus.RUNNING, null);

            // 테스트 설정 생성
            Map<String, Object> testConfig = configurationService.createTestConfiguration(request);

            // 설정 유효성 검증
            boolean isValid = configurationService.validateConfiguration(testConfig);

            if (!isValid) {
                throw new PerformanceTestException("테스트 설정이 유효하지 않습니다");
            }

            // 결과 디렉토리 준비
            String resultDir = configurationService.prepareResultDirectory((String) testConfig.get("resultDirectory"));

            // 시스템 속성으로 설정 전달
            testConfig.forEach((key, value) -> {
                System.setProperty("test." + key, String.valueOf(value));
            });
            
            // plan_id와 run_type을 시스템 속성으로 전달
            System.setProperty("plan_id", String.valueOf(request.getPlanId()));
            System.setProperty("run_type", request.getRunType() != null ? request.getRunType() : "TEST");
            System.setProperty("user_count", String.valueOf(request.getMaxUsers()));

            // Gradle을 통해 Gatling 실행
            ProcessBuilder processBuilder = new ProcessBuilder(
                "./gradlew", "gatlingRun",
                "-Dgatling.simulationClass=kr.co.iosys.exam.performance.simulation.ExamCenterSimulation",
                "-Dplan_id=" + request.getPlanId(),
                "-Drun_type=" + (request.getRunType() != null ? request.getRunType() : "TEST"),
                "-Duser_count=" + request.getMaxUsers(),
                "-Dgatling.core.directory.results=" + resultDir
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // 프로세스 출력 로깅
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Gatling: {}", line);
                }
            }
            
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Gatling 테스트 완료: {}", testId);
                updateTestStatus(testId, PerformanceTestResponse.TestStatus.COMPLETED, null);
                
                // 결과 파싱 및 저장 (추후 구현)
                parseAndStoreResults(testId, resultDir);
            } else {
                throw new PerformanceTestException("Gatling 테스트 실행 실패. Exit code: " + exitCode);
            }

        } catch (Exception e) {
            log.error("Gatling 테스트 실행 중 오류 발생: {}", testId, e);
            updateTestStatus(testId, PerformanceTestResponse.TestStatus.FAILED, e.getMessage());
        }
    }

    /**
     * 테스트 상태 업데이트
     */
    private void updateTestStatus(String testId, PerformanceTestResponse.TestStatus status, String errorMessage) {
        PerformanceTestResponse response = runningTests.get(testId);
        if (response != null) {
            response.setStatus(status);
            if (errorMessage != null) {
                response.setErrorMessage(errorMessage);
            }
            if (status == PerformanceTestResponse.TestStatus.COMPLETED ||
                status == PerformanceTestResponse.TestStatus.FAILED ||
                status == PerformanceTestResponse.TestStatus.CANCELLED) {
                response.setEndTime(LocalDateTime.now());
            }
        }
    }

    /**
     * 결과 파싱 및 저장 (향후 구현)
     */
    private void parseAndStoreResults(String testId, String resultDir) {
        // AIDEV-TODO: Gatling 결과 파일을 파싱하여 DB에 저장하는 로직 구현
        log.info("테스트 결과 저장 위치: {}", resultDir);
        
        PerformanceTestResponse response = runningTests.get(testId);
        if (response != null) {
            // 간단한 결과 정보 설정
            Map<String, Object> results = Map.of(
                "resultDirectory", resultDir,
                "completedAt", LocalDateTime.now().toString()
            );
            response.setResults(results);
        }
    }
}