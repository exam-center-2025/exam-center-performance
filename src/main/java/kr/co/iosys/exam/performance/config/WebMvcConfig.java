package kr.co.iosys.exam.performance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * AIDEV-NOTE: WebMvc 설정 클래스
 * CORS 설정, 정적 리소스 핸들링 및 기타 WebMvc 관련 설정을 담당
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // AIDEV-NOTE: Gatling 리포트를 정적 리소스로 서빙 - build/reports/gatling 디렉토리 매핑
        // /reports/** URL로 접근 가능하도록 설정
        registry.addResourceHandler("/reports/**")
                .addResourceLocations("file:./build/reports/")
                .setCachePeriod(0); // 캐시 비활성화 (개발 중)
        
        // 호환성을 위해 gatling-reports 경로도 추가
        registry.addResourceHandler("/gatling-reports/**")
                .addResourceLocations("file:./build/reports/gatling/")
                .setCachePeriod(0);
    }
}