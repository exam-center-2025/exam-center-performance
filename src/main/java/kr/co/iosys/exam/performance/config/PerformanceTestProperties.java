package kr.co.iosys.exam.performance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AIDEV-NOTE: 성능 테스트 관련 설정 프로퍼티
 * application.yml의 performance-test 섹션과 매핑
 */
@Data
@Validated
@ConfigurationProperties(prefix = "performance-test")
public class PerformanceTestProperties {

    @NotNull
    private Gatling gatling = new Gatling();

    @NotNull
    private Database database = new Database();

    @Data
    public static class Gatling {
        @NotBlank
        private String resultsDirectory = "./gatling-results";

        @NotBlank
        private String simulationsDirectory = "./gatling-simulations";

        @NotNull
        private Target target = new Target();
        
        @NotNull
        private Reports reports = new Reports();

        @Min(1)
        private int maxConcurrentTests = 3;

        @Min(1)
        private int testTimeoutMinutes = 30;
    }

    @Data
    public static class Target {
        @NotBlank
        private String baseUrl = "http://localhost:8091";

        @NotBlank
        private String websocketUrl = "ws://localhost:8099";
    }
    
    @Data
    public static class Reports {
        @NotBlank
        private String basePath = "/home/ldm/exam-center-dev/performance-reports";
        
        private boolean useDateFolder = true;
        
        private boolean useTestName = true;
    }

    @Data
    public static class Database {
        @NotBlank
        private String host = "192.168.100.105";

        @Min(1)
        private int port = 5432;

        @NotBlank
        private String database = "exam_db";

        @NotBlank
        private String username = "postgres";

        @NotBlank
        private String password = "postgres";

        public String getJdbcUrl() {
            return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        }
    }
}