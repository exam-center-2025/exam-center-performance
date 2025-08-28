package kr.co.iosys.exam.performance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * AIDEV-NOTE: 기본 애플리케이션 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
class PerformanceTestApplicationTests {

    @Test
    void contextLoads() {
        // Spring Boot 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인
    }
}