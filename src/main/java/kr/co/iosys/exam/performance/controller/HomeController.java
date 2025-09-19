package kr.co.iosys.exam.performance.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈 컨트롤러 - 루트 경로 처리
 * AIDEV-NOTE: 루트 경로 접근 시 대시보드로 자동 리다이렉트
 */
@Slf4j
@Controller
public class HomeController {
    
    /**
     * 루트 경로 접속 시 대시보드로 리다이렉트
     * GET /
     */
    @GetMapping("/")
    public String home() {
        log.info("루트 경로 접속 - 대시보드로 리다이렉트");
        return "redirect:/dashboard/configure?planId=1";
    }
    
    /**
     * 인덱스 페이지 (별칭)
     * GET /index
     */
    @GetMapping("/index")
    public String index() {
        return "redirect:/dashboard/configure?planId=1";
    }
}