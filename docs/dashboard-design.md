# 성능 대시보드 설계 문서

## 1. 개요

### 목적
Exam Center 성능 테스트를 관리하고 모니터링하기 위한 웹 기반 대시보드

### 기술 스택
- **Backend**: Spring Boot + Thymeleaf
- **Frontend**: Bootstrap 5, Chart.js, WebSocket (SockJS/STOMP)
- **Database**: PostgreSQL (영구 저장), Redis (실시간 데이터)

## 2. 페이지 구성

### 2.1 메인 대시보드 (`/dashboard`)

#### 기능
- 시험 계획 목록 표시
- 실행 중인 테스트 상태 표시
- 최근 테스트 결과 요약

#### 화면 구성
```
┌─────────────────────────────────────────────────────────┐
│  Exam Center Performance Dashboard                       │
├─────────────────────────────────────────────────────────┤
│  [시험 계획 선택] [테스트 실행] [결과 보기]              │
├─────────────────────────────────────────────────────────┤
│  시험 계획 목록                                          │
│  ┌──────┬──────────┬────────┬──────────┬──────────┐    │
│  │  ID  │  계획명   │  상태   │  시작일   │   액션    │    │
│  ├──────┼──────────┼────────┼──────────┼──────────┤    │
│  │   1  │ 2024정기 │  준비   │ 01-15    │ [테스트] │    │
│  │   2  │ 모의고사 │  진행   │ 01-20    │ [테스트] │    │
│  └──────┴──────────┴────────┴──────────┴──────────┘    │
│                                                          │
│  실행 중인 테스트                                        │
│  ┌────────────────┬─────────┬──────────────────────┐   │
│  │    테스트 ID    │  진행률  │        상태          │   │
│  ├────────────────┼─────────┼──────────────────────┤   │
│  │ Test_1_183842  │   65%   │  ████████░░░░  실행중│   │
│  └────────────────┴─────────┴──────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 2.2 테스트 설정 (`/dashboard/configure`)

#### 기능
- 테스트 파라미터 설정
- 시나리오 선택
- 테스트 실행

#### 화면 구성
```
┌─────────────────────────────────────────────────────────┐
│  테스트 설정                                             │
├─────────────────────────────────────────────────────────┤
│  시험 계획: [선택 ▼]                                     │
│  Run Type:  [TEST ▼]                                     │
│                                                          │
│  ◆ 부하 설정                                            │
│  최대 사용자:     [___100] 명                           │
│  램프업 시간:     [____60] 초                           │
│  테스트 시간:     [___300] 초                           │
│                                                          │
│  ◆ 시나리오                                             │
│  ○ BASIC    - 기본 테스트                               │
│  ● COMPLETE - 전체 시나리오                             │
│  ○ STRESS   - 스트레스 테스트                           │
│                                                          │
│  [테스트 시작] [템플릿 저장]                             │
└─────────────────────────────────────────────────────────┘
```

### 2.3 실시간 모니터링 (`/dashboard/monitor/{testId}`)

#### 기능
- 실시간 메트릭 표시 (WebSocket)
- 진행 상태 표시
- 테스트 제어 (중단)

#### 화면 구성
```
┌─────────────────────────────────────────────────────────┐
│  실시간 모니터링 - Test_1_183842                         │
├─────────────────────────────────────────────────────────┤
│  진행률: ████████████░░░░░░  65% (195/300초)            │
│                                                          │
│  ┌─────────────────────┬─────────────────────┐         │
│  │      TPS 그래프      │    응답시간 그래프   │         │
│  │   📈 1,250 tps       │   📊 245ms avg      │         │
│  └─────────────────────┴─────────────────────┘         │
│                                                          │
│  ┌─────────────────────┬─────────────────────┐         │
│  │    활성 사용자 수    │      에러율         │         │
│  │   👥 85/100         │   ⚠️ 0.24%         │         │
│  └─────────────────────┴─────────────────────┘         │
│                                                          │
│  실시간 로그                                             │
│  ┌─────────────────────────────────────────────────┐   │
│  │ [18:45:01] User 1 connected                     │   │
│  │ [18:45:02] Test started                         │   │
│  │ [18:45:03] Ramping up users...                  │   │
│  └─────────────────────────────────────────────────┘   │
│                                                          │
│  [테스트 중단]                                           │
└─────────────────────────────────────────────────────────┘
```

### 2.4 결과 보기 (`/dashboard/results/{testId}`)

#### 기능
- 테스트 결과 요약
- 상세 메트릭 표시
- Gatling 리포트 링크

#### 화면 구성
```
┌─────────────────────────────────────────────────────────┐
│  테스트 결과 - Test_1_183842                             │
├─────────────────────────────────────────────────────────┤
│  ◆ 요약                                                 │
│  테스트 시간:    300초                                   │
│  총 요청:        36,150                                  │
│  성공률:         99.76%                                  │
│  평균 응답시간:  245ms                                   │
│  최대 TPS:       1,450                                   │
│                                                          │
│  ◆ 응답시간 분포                                        │
│  최소:    12ms                                          │
│  P50:     198ms                                         │
│  P95:     512ms                                         │
│  P99:     890ms                                         │
│  최대:    1,890ms                                       │
│                                                          │
│  ◆ 에러 분석                                            │
│  총 에러:     87                                         │
│  타임아웃:    45                                         │
│  연결 실패:   42                                         │
│                                                          │
│  [Gatling 리포트 보기] [CSV 다운로드]                    │
└─────────────────────────────────────────────────────────┘
```

## 3. API 엔드포인트

### 3.1 페이지 라우트
```
GET  /dashboard                        - 메인 대시보드
GET  /dashboard/configure              - 테스트 설정
GET  /dashboard/monitor/{testId}       - 실시간 모니터링
GET  /dashboard/results/{testId}       - 결과 보기
GET  /dashboard/results/{testId}/report - Gatling 리포트 (iframe)
```

### 3.2 REST API
```
GET  /api/dashboard/plans              - 시험 계획 목록
GET  /api/dashboard/tests/active       - 실행 중인 테스트
GET  /api/dashboard/tests/recent       - 최근 테스트 결과
POST /api/dashboard/tests/start        - 테스트 시작
POST /api/dashboard/tests/{id}/stop    - 테스트 중단
GET  /api/dashboard/tests/{id}/metrics - 테스트 메트릭
GET  /api/dashboard/templates          - 템플릿 목록
POST /api/dashboard/templates          - 템플릿 저장
```

### 3.3 WebSocket
```
/ws                                    - WebSocket 엔드포인트
/topic/metrics/{testId}                - 실시간 메트릭 구독
/topic/logs/{testId}                   - 실시간 로그 구독
/topic/status/{testId}                 - 상태 업데이트 구독
```

## 4. 컨트롤러 구조

### 4.1 DashboardController (Thymeleaf)
```java
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    
    @GetMapping
    public String dashboard(Model model)
    
    @GetMapping("/configure")
    public String configure(Model model)
    
    @GetMapping("/monitor/{testId}")
    public String monitor(@PathVariable String testId, Model model)
    
    @GetMapping("/results/{testId}")
    public String results(@PathVariable String testId, Model model)
}
```

### 4.2 DashboardApiController (REST)
```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {
    
    @GetMapping("/plans")
    public List<ExamPlan> getPlans()
    
    @PostMapping("/tests/start")
    public TestResponse startTest(@RequestBody TestRequest request)
    
    @GetMapping("/tests/{id}/metrics")
    public TestMetrics getMetrics(@PathVariable String id)
}
```

### 4.3 DashboardWebSocketController
```java
@Controller
public class DashboardWebSocketController {
    
    @MessageMapping("/test/{testId}/subscribe")
    @SendTo("/topic/metrics/{testId}")
    public TestMetrics subscribeToMetrics(@DestinationVariable String testId)
}
```

## 5. 데이터 모델

### 5.1 DTO
```java
// 테스트 요청
public class TestRequest {
    private Long planId;
    private String runType;
    private Integer maxUsers;
    private Integer rampUpSeconds;
    private Integer testDurationSeconds;
    private String scenario;
}

// 테스트 메트릭
public class TestMetrics {
    private String testId;
    private Long timestamp;
    private Integer activeUsers;
    private Double tps;
    private Double avgResponseTime;
    private Double errorRate;
    private Double progress;
}

// 테스트 결과
public class TestResult {
    private String testId;
    private Long totalRequests;
    private Long successfulRequests;
    private Double successRate;
    private Double avgResponseTime;
    private Double maxTps;
    private String reportPath;
}
```

## 6. 실시간 업데이트 구현

### 6.1 WebSocket 설정
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```

### 6.2 메트릭 브로드캐스트 서비스
```java
@Service
public class MetricsBroadcastService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Scheduled(fixedDelay = 1000)
    public void broadcastMetrics() {
        Set<String> activeTests = redisTemplate.opsForSet()
            .members("tests:active");
            
        for (String testId : activeTests) {
            TestMetrics metrics = getMetricsFromRedis(testId);
            messagingTemplate.convertAndSend(
                "/topic/metrics/" + testId, 
                metrics
            );
        }
    }
}
```

## 7. 보안 고려사항

### 7.1 인증/인가
- Spring Security 기본 인증 적용
- 역할 기반 접근 제어 (ROLE_USER, ROLE_ADMIN)

### 7.2 CSRF 보호
- Thymeleaf 폼에 CSRF 토큰 포함
- AJAX 요청 시 헤더에 토큰 전달

### 7.3 WebSocket 보안
- STOMP 헤더 인증
- 세션 기반 접근 제어

## 8. 성능 최적화

### 8.1 캐싱 전략
- 시험 계획 목록: 5분 캐시
- 테스트 템플릿: 10분 캐시
- 정적 리소스: 브라우저 캐시 활용

### 8.2 데이터 전송 최적화
- WebSocket 메시지 압축
- 메트릭 데이터 샘플링 (1초 간격)
- 필요한 필드만 선택적 전송

## 9. 에러 처리

### 9.1 사용자 친화적 메시지
- 테스트 실패 시 명확한 에러 메시지
- 네트워크 오류 시 재연결 시도
- 타임아웃 처리

### 9.2 복구 전략
- WebSocket 자동 재연결
- 실패한 테스트 재시도 옵션
- 부분 실패 시 계속 진행 옵션