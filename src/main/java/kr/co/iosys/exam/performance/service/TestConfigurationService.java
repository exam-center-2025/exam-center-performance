package kr.co.iosys.exam.performance.service;

import kr.co.iosys.exam.performance.config.PerformanceTestProperties;
import kr.co.iosys.exam.performance.dto.PerformanceTestRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * AIDEV-NOTE: 성능 테스트 설정 관리 서비스
 * Gatling 테스트 실행을 위한 설정 생성 및 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestConfigurationService {

    private final PerformanceTestProperties properties;

    /**
     * 성능 테스트 실행 설정 생성
     */
    public Map<String, Object> createTestConfiguration(PerformanceTestRequest request) {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // 기본 설정
            config.put("testName", request.getTestName());
            config.put("planId", request.getPlanId());
            config.put("maxUsers", request.getMaxUsers());
            config.put("rampUpDurationSeconds", request.getRampUpDurationSeconds());
            config.put("testDurationSeconds", request.getTestDurationSeconds());
            config.put("runType", request.getRunType());
            config.put("scenario", request.getScenario());
            
            // 타겟 서버 설정
            config.put("baseUrl", properties.getGatling().getTarget().getBaseUrl());
            config.put("websocketUrl", properties.getGatling().getTarget().getWebsocketUrl());
            
            // 데이터베이스 설정
            PerformanceTestProperties.Database dbConfig = properties.getDatabase();
            config.put("dbUrl", dbConfig.getJdbcUrl());
            config.put("dbUsername", dbConfig.getUsername());
            config.put("dbPassword", dbConfig.getPassword());
            
            // 결과 디렉토리 설정 - 설정 파일의 리포트 옵션 사용
            String resultDir = buildReportPath(request);
            config.put("resultDirectory", resultDir);
            
            // 추가 설정 병합
            if (request.getAdditionalConfig() != null) {
                config.putAll(request.getAdditionalConfig());
            }
            
            log.info("성능 테스트 설정 생성 완료: {}", config);
            return config;
        } catch (Exception e) {
            log.error("성능 테스트 설정 생성 실패", e);
            throw new RuntimeException("성능 테스트 설정 생성 실패", e);
        }
    }

    /**
     * 결과 디렉토리 준비
     */
    public String prepareResultDirectory(String resultDirectory) {
        try {
            Path resultPath = Paths.get(resultDirectory);
            Files.createDirectories(resultPath);
            log.info("결과 디렉토리 생성: {}", resultPath.toAbsolutePath());
            return resultPath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("결과 디렉토리 생성 실패: {}", resultDirectory, e);
            throw new RuntimeException("결과 디렉토리 생성 실패", e);
        }
    }

    /**
     * 시뮬레이션 디렉토리 준비
     */
    public String prepareSimulationDirectory() {
        try {
            Path simulationPath = Paths.get(properties.getGatling().getSimulationsDirectory());
            Files.createDirectories(simulationPath);
            log.info("시뮬레이션 디렉토리 확인: {}", simulationPath.toAbsolutePath());
            return simulationPath.toAbsolutePath().toString();
        } catch (Exception e) {
            log.error("시뮬레이션 디렉토리 준비 실패", e);
            throw new RuntimeException("시뮬레이션 디렉토리 준비 실패", e);
        }
    }

    /**
     * 테스트 ID 생성
     */
    public String generateTestId(PerformanceTestRequest request) {
        return String.format("%s_%d_%s", 
            request.getTestName().replaceAll("[^a-zA-Z0-9]", "_"),
            request.getPlanId(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
    }

    /**
     * 설정 유효성 검증
     */
    public boolean validateConfiguration(Map<String, Object> config) {
        try {
            // 필수 설정 확인
            String[] requiredKeys = {
                "testName", "planId", "maxUsers", "baseUrl", "websocketUrl"
            };
            
            for (String key : requiredKeys) {
                if (!config.containsKey(key) || config.get(key) == null) {
                    log.error("필수 설정 누락: {}", key);
                    return false;
                }
            }
            
            // 수치 설정 검증
            Integer maxUsers = (Integer) config.get("maxUsers");
            if (maxUsers <= 0) {
                log.error("잘못된 사용자 수: {}", maxUsers);
                return false;
            }
            
            log.info("설정 유효성 검증 통과");
            return true;
        } catch (Exception e) {
            log.error("설정 유효성 검증 실패", e);
            return false;
        }
    }
    
    /**
     * 리포트 경로 생성 - 설정 파일의 옵션 적용
     */
    private String buildReportPath(PerformanceTestRequest request) {
        PerformanceTestProperties.Reports reportConfig = properties.getGatling().getReports();
        StringBuilder pathBuilder = new StringBuilder(reportConfig.getBasePath());
        
        // 날짜별 폴더 사용 여부
        if (reportConfig.isUseDateFolder()) {
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            pathBuilder.append("/").append(dateFolder);
        }
        
        // 테스트명별 폴더 사용 여부
        if (reportConfig.isUseTestName()) {
            String testNameFolder = request.getTestName().replaceAll("[^a-zA-Z0-9]", "_");
            pathBuilder.append("/").append(testNameFolder);
        }
        
        // 타임스탬프 추가 (항상 포함하여 고유성 보장)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        pathBuilder.append("_").append(request.getPlanId());
        pathBuilder.append("_").append(timestamp);
        
        String resultPath = pathBuilder.toString();
        log.info("리포트 경로 생성: {}", resultPath);
        return resultPath;
    }
}