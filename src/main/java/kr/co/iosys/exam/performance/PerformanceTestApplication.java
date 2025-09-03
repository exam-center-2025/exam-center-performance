package kr.co.iosys.exam.performance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AIDEV-NOTE: 시험 센터 성능 테스트 서비스 메인 애플리케이션
 * Gatling을 사용한 프로그래매틱 성능 테스트 실행을 담당
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan("kr.co.iosys.exam.performance.config")
public class PerformanceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceTestApplication.class, args);
    }
}